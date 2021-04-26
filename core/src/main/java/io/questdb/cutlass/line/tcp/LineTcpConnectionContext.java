/*******************************************************************************
 *     ___                  _   ____  ____
 *    / _ \ _   _  ___  ___| |_|  _ \| __ )
 *   | | | | | | |/ _ \/ __| __| | | |  _ \
 *   | |_| | |_| |  __/\__ \ |_| |_| | |_) |
 *    \__\_\\__,_|\___||___/\__|____/|____/
 *
 *  Copyright (c) 2014-2019 Appsicle
 *  Copyright (c) 2019-2020 QuestDB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package io.questdb.cutlass.line.tcp;

import io.questdb.cutlass.line.tcp.LineTcpMeasurementScheduler.NetworkIOJob;
import io.questdb.cutlass.line.tcp.NewLineProtoParser.ParseResult;
import io.questdb.log.Log;
import io.questdb.log.LogFactory;
import io.questdb.network.IOContext;
import io.questdb.network.IODispatcher;
import io.questdb.network.NetworkFacade;
import io.questdb.std.Mutable;
import io.questdb.std.Unsafe;
import io.questdb.std.Vect;
import io.questdb.std.datetime.millitime.MillisecondClock;
import io.questdb.std.str.DirectByteCharSequence;
import io.questdb.std.str.FloatingDirectCharSink;

class LineTcpConnectionContext implements IOContext, Mutable {
    private static final Log LOG = LogFactory.getLog(LineTcpConnectionContext.class);
    private static final long QUEUE_FULL_LOG_HYSTERESIS_IN_MS = 10_000;
    protected final NetworkFacade nf;
    private final LineTcpMeasurementScheduler scheduler;
    private final MillisecondClock milliClock;
    private final DirectByteCharSequence byteCharSequence = new DirectByteCharSequence();
    protected long fd;
    protected IODispatcher<LineTcpConnectionContext> dispatcher;
    protected long recvBufStart;
    protected long recvBufEnd;
    protected long recvBufPos;
    protected boolean peerDisconnected;
    private long lastQueueFullLogMillis = 0;
    private final NewLineProtoParser protoParser = new NewLineProtoParser();
    private boolean goodMeasurement;
    protected long recvBufStartOfMeasurement;
    private final FloatingDirectCharSink charSink = new FloatingDirectCharSink();

    LineTcpConnectionContext(LineTcpReceiverConfiguration configuration, LineTcpMeasurementScheduler scheduler) {
        nf = configuration.getNetworkFacade();
        this.scheduler = scheduler;
        this.milliClock = configuration.getMillisecondClock();
        recvBufStart = Unsafe.malloc(configuration.getNetMsgBufferSize());
        recvBufEnd = recvBufStart + configuration.getNetMsgBufferSize();
        clear();
    }

    @Override
    public void clear() {
        recvBufPos = recvBufStart;
        peerDisconnected = false;
        resetParser();
    }

    private void resetParser() {
        protoParser.of(recvBufStart);
        goodMeasurement = true;
        recvBufStartOfMeasurement = recvBufStart;
    }

    @Override
    public void close() {
        this.fd = -1;
        Unsafe.free(recvBufStart, recvBufEnd - recvBufStart);
        recvBufStart = recvBufEnd = recvBufPos = 0;
        protoParser.close();
        charSink.close();
    }

    @Override
    public long getFd() {
        return fd;
    }

    @Override
    public boolean invalid() {
        return fd == -1;
    }

    @Override
    public IODispatcher<LineTcpConnectionContext> getDispatcher() {
        return dispatcher;
    }

    private boolean checkQueueFullLogHysteresis() {
        long millis = milliClock.getTicks();
        if ((millis - lastQueueFullLogMillis) >= QUEUE_FULL_LOG_HYSTERESIS_IN_MS) {
            lastQueueFullLogMillis = millis;
            return true;
        }
        return false;
    }

    protected final boolean compactBuffer(long recvBufNewStart) {
        assert recvBufNewStart <= recvBufPos;
        if (recvBufNewStart > recvBufStart) {
            final int len = (int) (recvBufPos - recvBufNewStart);
            if (len > 0) {
                Vect.memcpy(recvBufNewStart, recvBufStart, len);
            }
            recvBufPos = recvBufStart + len;
            return true;
        }
        return false;
    }

    private boolean doHandleDisconnectEvent() {
        if (protoParser.getBufferAddress() == recvBufEnd) {
            LOG.error().$('[').$(fd).$("] buffer overflow [msgBufferSize=").$(recvBufEnd - recvBufStart).$(']').$();
            return true;
        }

        if (peerDisconnected) {
            // Peer disconnected, we have now finished disconnect our end
            if (recvBufPos != recvBufStart) {
                LOG.info().$('[').$(fd).$("] peer disconnected with partial measurement, ").$(recvBufPos - recvBufStart).$(" unprocessed bytes").$();
            } else {
                LOG.info().$('[').$(fd).$("] peer disconnected").$();
            }
        }
        return peerDisconnected;
    }

    IOContextResult handleIO(NetworkIOJob netIoJob) {
        read();
        return parseMeasurements(netIoJob);
    }

    protected final IOContextResult parseMeasurements(NetworkIOJob netIoJob) {
        while (true) {
            try {
                ParseResult rc = goodMeasurement ? protoParser.parseMeasurement(recvBufPos) : protoParser.skipMeasurement(recvBufPos);
                switch (rc) {
                    case MEASUREMENT_COMPLETE: {
                        if (goodMeasurement) {
                            if (!scheduler.tryCommitNewEvent(netIoJob, protoParser, charSink)) {
                                // Waiting for writer threads to drain queue, request callback as soon as possible
                                if (checkQueueFullLogHysteresis()) {
                                    LOG.debug().$('[').$(fd).$("] queue full").$();
                                }
                                return IOContextResult.QUEUE_FULL;
                            }
                        } else {
                            int position = (int) (protoParser.getBufferAddress() - recvBufStartOfMeasurement);
                            LOG.error().$('[').$(fd).$("] could not parse measurement, code ").$(protoParser.getErrorCode()).$(" at ").$(position)
                                    .$(" line (may be mangled due to partial parsing) is ")
                                    .$(byteCharSequence.of(recvBufStartOfMeasurement, protoParser.getBufferAddress())).$();
                            goodMeasurement = true;
                        }
                        protoParser.startNextMeasurement();
                        recvBufStartOfMeasurement = protoParser.getBufferAddress();
                        if (recvBufStartOfMeasurement == recvBufPos) {
                            recvBufPos = recvBufStart;
                            protoParser.of(recvBufStart);
                        }
                        continue;
                    }

                    case ERROR: {
                        goodMeasurement = false;
                        continue;
                    }

                    case BUFFER_UNDERFLOW: {
                        if (recvBufPos == recvBufEnd) {
                            if (!compactBuffer(recvBufStartOfMeasurement)) {
                                doHandleDisconnectEvent();
                                return IOContextResult.NEEDS_DISCONNECT;
                            }
                            resetParser();
                        }
                        if (!read()) {
                            if (peerDisconnected) {
                                return IOContextResult.NEEDS_DISCONNECT;
                            }
                            return IOContextResult.NEEDS_READ;
                        }
                        break;
                    }
                }
            } catch (RuntimeException ex) {
                LOG.error().$('[').$(fd).$("] could not process line data").$(ex).$();
                return IOContextResult.NEEDS_DISCONNECT;
            }
        }
    }

    LineTcpConnectionContext of(long clientFd, IODispatcher<LineTcpConnectionContext> dispatcher) {
        this.fd = clientFd;
        this.dispatcher = dispatcher;
        clear();
        return this;
    }

    protected final boolean read() {
        int bufferRemaining = (int) (recvBufEnd - recvBufPos);
        final int orig = bufferRemaining;
        if (bufferRemaining > 0 && !peerDisconnected) {
            int nRead = nf.recv(fd, recvBufPos, bufferRemaining);
            if (nRead > 0) {
                recvBufPos += nRead;
                bufferRemaining -= nRead;
            } else {
                peerDisconnected = nRead < 0;
            }
            return bufferRemaining < orig;
        }
        return !peerDisconnected;
    }

    enum IOContextResult {
        NEEDS_READ, NEEDS_WRITE, QUEUE_FULL, NEEDS_DISCONNECT
    }
}