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

import io.questdb.cutlass.line.tcp.LineTcpMeasurementScheduler.LineTcpMeasurementEvent;
import io.questdb.log.Log;
import io.questdb.log.LogFactory;
import io.questdb.network.IOContext;
import io.questdb.network.IODispatcher;
import io.questdb.network.IOOperation;
import io.questdb.network.NetworkFacade;
import io.questdb.std.Mutable;
import io.questdb.std.Unsafe;
import io.questdb.std.str.DirectByteCharSequence;
import io.questdb.std.time.MillisecondClock;

class LineTcpConnectionContext implements IOContext, Mutable {
    private static final Log LOG = LogFactory.getLog(LineTcpConnectionContext.class);
    private static final long QUEUE_FULL_LOG_HYSTERESIS_IN_MS = 10_000;
    private final NetworkFacade nf;
    private final LineTcpMeasurementScheduler scheduler;
    private final MillisecondClock milliClock;
    private long fd;
    private IODispatcher<LineTcpConnectionContext> dispatcher;
    private long recvBufStart;
    private long recvBufEnd;
    private long recvBufPos;
    private boolean peerDisconnected;
    private final DirectByteCharSequence byteCharSequence = new DirectByteCharSequence();
    private long lastQueueFullLogMillis = 0;

    LineTcpConnectionContext(LineTcpReceiverConfiguration configuration, LineTcpMeasurementScheduler scheduler, MillisecondClock clock) {
        nf = configuration.getNetworkFacade();
        this.scheduler = scheduler;
        this.milliClock = clock;
        recvBufStart = Unsafe.malloc(configuration.getNetMsgBufferSize());
        recvBufEnd = recvBufStart + configuration.getNetMsgBufferSize();
    }

    // returns true if busy
    boolean handleIO() {
        try {
            // Read as much data as possible
            int len = (int) (recvBufEnd - recvBufPos);
            while (len > 0 && !peerDisconnected) {
                int nRead = nf.recv(fd, recvBufPos, len);
                if (nRead < 0) {
                    if (recvBufPos != recvBufStart) {
                        LOG.info().$('[').$(fd).$("] peer disconnected with partial measurement, ").$(recvBufPos - recvBufStart).$(" unprocessed bytes").$();
                    } else {
                        LOG.info().$('[').$(fd).$("] peer disconnected").$();
                    }
                    peerDisconnected = true;
                } else {
                    if (nRead > 0) {
                        recvBufPos += nRead;
                        len -= nRead;
                    } else {
                        break;
                    }
                }
            }

            // Process as much data as possible
            long recvBufLineStart = recvBufStart;
            boolean queueFull = false;
            do {
                LineTcpMeasurementEvent event = scheduler.getNewEvent();
                if (null == event) {
                    // Waiting for writer threads to drain queue, request callback as soon as possible
                    if (checkQueueFullLogHysteresis()) {
                        LOG.info().$('[').$(fd).$("] queue full, consider increasing queue size or number of writer jobs").$();
                    }
                    queueFull = true;
                    break;
                }
                boolean complete = false;
                try {
                    long recvBufLineNext = event.parseLine(recvBufLineStart, recvBufPos);
                    if (recvBufLineNext == -1) {
                        break;
                    }
                    if (event.isComplete()) {
                        complete = true;
                    } else {
                        LOG.error().$('[').$(fd).$("] failed to parse measurement, code ").$(event.getErrorCode()).$(" at ").$(event.getErrorPosition()).$(" in ")
                                .$(byteCharSequence.of(recvBufLineStart, recvBufLineNext - 1)).$();
                    }
                    recvBufLineStart = recvBufLineNext;
                } finally {
                    scheduler.commitNewEvent(event, complete);
                }
            } while (recvBufLineStart != recvBufPos);

            // Compact input buffer
            if (recvBufLineStart != recvBufStart) {
                len = (int) (recvBufPos - recvBufLineStart);
                if (len > 0) {
                    Unsafe.getUnsafe().copyMemory(recvBufLineStart, recvBufStart, len);
                }
                recvBufPos = recvBufStart + len;
            }

            if (queueFull) {
                return true;
            }

            // Check for buffer overflow
            if (recvBufPos == recvBufEnd) {
                LOG.error().$('[').$(fd).$("] buffer overflow [msgBufferSize=").$(recvBufEnd - recvBufStart).$(']').$();
                dispatcher.disconnect(this);
                return false;
            }

            if (peerDisconnected) {
                // Peer disconnected, we have now finished disconnect our end
                dispatcher.disconnect(this);
                return false;
            }

            dispatcher.registerChannel(this, IOOperation.READ);
            return false;
        } catch (RuntimeException ex) {
            LOG.error().$('[').$(fd).$("] Failed to process line data").$(ex).$();
            dispatcher.disconnect(this);
            return false;
        }
    }

    private boolean checkQueueFullLogHysteresis() {
        long millis = milliClock.getTicks();
        if ((millis - lastQueueFullLogMillis) >= QUEUE_FULL_LOG_HYSTERESIS_IN_MS) {
            lastQueueFullLogMillis = millis;
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        recvBufPos = recvBufStart;
        peerDisconnected = false;
    }

    @Override
    public void close() {
        this.fd = -1;
        Unsafe.free(recvBufStart, recvBufEnd - recvBufStart);
        recvBufStart = recvBufEnd = recvBufPos = 0;
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

    LineTcpConnectionContext of(long clientFd, IODispatcher<LineTcpConnectionContext> dispatcher) {
        this.fd = clientFd;
        this.dispatcher = dispatcher;
        clear();
        return this;
    }
}