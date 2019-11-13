/*******************************************************************************
 *    ___                  _   ____  ____
 *   / _ \ _   _  ___  ___| |_|  _ \| __ )
 *  | | | | | | |/ _ \/ __| __| | | |  _ \
 *  | |_| | |_| |  __/\__ \ |_| |_| | |_) |
 *   \__\_\\__,_|\___||___/\__|____/|____/
 *
 * Copyright (C) 2014-2019 Appsicle
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/

package io.questdb.cairo;

import io.questdb.griffin.engine.TestBinarySequence;
import io.questdb.std.*;
import io.questdb.std.str.StringSink;
import io.questdb.test.tools.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class VirtualMemoryTest {

    @Test
    public void testBinSequence() {
        testBinSequence0(700, 2048);
    }

    @Test
    public void testBinSequence2() {
        testBinSequence0(1024, 600);
    }

    @Test
    public void testBinSequenceOnEdge() {
        final Rnd rnd = new Rnd();
        try (VirtualMemory mem = new VirtualMemory(32)) {
            TestRecord.ArrayBinarySequence seq = new TestRecord.ArrayBinarySequence();
            int N = 33;
            int O = 10;

            seq.of(rnd.nextBytes(N));
            mem.putBin(seq);

            BinarySequence actual = mem.getBin(0);
            assertNotNull(actual);

            TestUtils.assertEquals(seq, actual, N);

            long buffer = Unsafe.malloc(1024);
            try {
                // supply length of our buffer
                // blob content would be shorter
                Unsafe.getUnsafe().setMemory(buffer, 1024, (byte) 5);
                actual.copyTo(buffer, 0, 1024);

                for (int i = 0; i < N; i++) {
                    assertEquals(seq.byteAt(i), Unsafe.getUnsafe().getByte(buffer + i));
                }

                // rest of the buffer must not be overwritten
                for (int i = N; i < 1024; i++) {
                    assertEquals(5, Unsafe.getUnsafe().getByte(buffer + i));
                }

                // copy from middle
                Unsafe.getUnsafe().setMemory(buffer, 1024, (byte) 5);
                actual.copyTo(buffer, O, 1024);

                for (int i = 0; i < N - O; i++) {
                    assertEquals(seq.byteAt(i + O), Unsafe.getUnsafe().getByte(buffer + i));
                }

                // rest of the buffer must not be overwritten
                for (int i = N - O; i < 1024; i++) {
                    assertEquals(5, Unsafe.getUnsafe().getByte(buffer + i));
                }
            } finally {
                Unsafe.free(buffer, 1024);
            }
        }
    }

    @Test
    public void testBool() {
        Rnd rnd = new Rnd();
        try (VirtualMemory mem = new VirtualMemory(11)) {
            int n = 120;

            for (int i = 0; i < n; i++) {
                mem.putBool(rnd.nextBoolean());
            }

            long o = 0;
            rnd.reset();
            for (int i = 0; i < n; i++) {
                assertEquals(rnd.nextBoolean(), mem.getBool(o++));
            }
        }
    }

    @Test
    public void testBoolRnd() {
        Rnd rnd = new Rnd();
        try (VirtualMemory mem = new VirtualMemory(11)) {
            int n = 120;
            long o = 0;

            for (int i = 0; i < n; i++) {
                mem.putBool(o++, rnd.nextBoolean());
            }

            o = 0;
            rnd.reset();
            for (int i = 0; i < n; i++) {
                assertEquals(rnd.nextBoolean(), mem.getBool(o++));
            }
        }
    }

    @Test
    public void testBulkCopy() {
        int N = 1000;
        try (VirtualMemory mem = new VirtualMemory(128)) {
            for (int i = 0; i < N; i++) {
                mem.putShort((short) i);
            }

            long target = N * 2;
            long offset = 0;
            short i = 0;
            while (target > 0) {
                long len = mem.pageRemaining(offset);
                target -= len;
                long address = mem.addressOf(offset);
                offset += len;
                while (len > 0 & i < N) {
                    assertEquals(i++, Unsafe.getUnsafe().getShort(address));
                    address += 2;
                    len -= 2;
                }
            }
        }
    }

    @Test
    public void testByte() {
        try (VirtualMemory mem = new VirtualMemory(11)) {
            int n = 120;

            for (int i = 0; i < n; i++) {
                mem.putByte((byte) i);
            }

            long o = 0;
            for (int i = 0; i < n; i++) {
                assertEquals(i, mem.getByte(o++));
            }
        }
    }

    @Test
    public void testLong256() {
        try (VirtualMemory mem = new VirtualMemory(256)) {
            mem.putLong256("0xEA674fdDe714fd979de3EdF0F56AA9716B898ec8");
            mem.putLong256("0xEA674fdDe714fd979de3EdF0F56AA9716B898ec8");
        }
    }

    @Test
    public void testByteRandom() {
        try (VirtualMemory mem = new VirtualMemory(128)) {
            long offset1 = 512;
            mem.putByte(offset1, (byte) 3);
            mem.putByte(offset1 + 1, (byte) 4);
            mem.jumpTo(offset1 + 2);
            mem.putByte((byte) 5);
            assertEquals(3, mem.getByte(offset1));
            assertEquals(4, mem.getByte(offset1 + 1));
            assertEquals(5, mem.getByte(offset1 + 2));
        }
    }

    @Test
    public void testByteRnd() {
        try (VirtualMemory mem = new VirtualMemory(11)) {
            int n = 120;

            long o = 0;
            for (int i = 0; i < n; i++, o++) {
                mem.putByte(o, (byte) i);
            }

            o = 0;
            for (int i = 0; i < n; i++) {
                assertEquals(i, mem.getByte(o++));
            }
        }
    }

    @Test
    public void testDouble() {
        try (VirtualMemory mem = new VirtualMemory(11)) {
            Rnd rnd = new Rnd();
            int n = 999;

            mem.putByte((byte) 1);

            for (int i = 0; i < n; i++) {
                mem.putDouble(rnd.nextDouble());
            }

            assertEquals(7993, mem.getAppendOffset());

            rnd.reset();
            long o = 1;
            assertEquals(1, mem.getByte(0));
            for (int i = 0; i < n; i++) {
                assertEquals(rnd.nextDouble(), mem.getDouble(o), 0.00001);
                o += 8;
            }
        }
    }

    @Test
    public void testLong256Direct() {
        long pageSize = 64;
        Rnd rnd = new Rnd();
        Long256Impl sink = new Long256Impl();
        try (VirtualMemory mem = new VirtualMemory(pageSize)) {
            for (int i = 0; i < 1000; i++) {
                mem.putLong256(rnd.nextLong(), rnd.nextLong(), rnd.nextLong(), rnd.nextLong());
            }

            rnd.reset();
            long offset = 0;
            for (int i = 0; i < 1000; i++) {
                mem.getLong256(offset, sink);
                offset += Long256.BYTES;
                Assert.assertEquals(rnd.nextLong(), sink.getLong0());
                Assert.assertEquals(rnd.nextLong(), sink.getLong1());
                Assert.assertEquals(rnd.nextLong(), sink.getLong2());
                Assert.assertEquals(rnd.nextLong(), sink.getLong3());
            }
        }
    }

    @Test
    public void testLong256Obj() {
        long pageSize = 64;
        Rnd rnd = new Rnd();
        Long256Impl long256 = new Long256Impl();
        try (VirtualMemory mem = new VirtualMemory(pageSize)) {
            for (int i = 0; i < 1000; i++) {
                long256.setLong0(rnd.nextLong());
                long256.setLong1(rnd.nextLong());
                long256.setLong2(rnd.nextLong());
                long256.setLong3(rnd.nextLong());
                mem.putLong256(long256);
            }

            rnd.reset();
            long offset = 0;
            for (int i = 0; i < 1000; i++) {
                mem.getLong256(offset, long256);
                offset += Long256.BYTES;
                Assert.assertEquals(rnd.nextLong(), long256.getLong0());
                Assert.assertEquals(rnd.nextLong(), long256.getLong1());
                Assert.assertEquals(rnd.nextLong(), long256.getLong2());
                Assert.assertEquals(rnd.nextLong(), long256.getLong3());
            }
        }
    }

    @Test
    public void testLong256Null() {
        long pageSize = 64;
        final int N = 1000;
        Long256Impl long256 = new Long256Impl();
        try (VirtualMemory mem = new VirtualMemory(pageSize)) {
            for (int i = 0; i < N; i++) {
                mem.putLong256((CharSequence) null);
            }

            StringSink sink = new StringSink();
            long offset = 0;
            for (int i = 0; i < N; i++) {
                mem.getLong256(offset, long256);
                Assert.assertEquals(-1L, long256.getLong0());
                Assert.assertEquals(-1L, long256.getLong1());
                Assert.assertEquals(-1L, long256.getLong2());
                Assert.assertEquals(-1L, long256.getLong3());
                mem.getLong256(offset, sink);
                Assert.assertEquals(0, sink.length());
                offset += Long256.BYTES;
            }
        }
    }

    @Test
    public void testLong256ObjExternallySequenced() {
        long pageSize = 64;
        Rnd rnd = new Rnd();
        long offset = 0;
        Long256Impl long256 = new Long256Impl();
        try (VirtualMemory mem = new VirtualMemory(pageSize)) {
            for (int i = 0; i < 1000; i++) {
                long256.setLong0(rnd.nextLong());
                long256.setLong1(rnd.nextLong());
                long256.setLong2(rnd.nextLong());
                long256.setLong3(rnd.nextLong());
                mem.putLong256(offset, long256);
                offset += Long256.BYTES;
            }

            rnd.reset();
            offset = 0;
            for (int i = 0; i < 1000; i++) {
                mem.getLong256(offset, long256);
                offset += Long256.BYTES;
                Assert.assertEquals(rnd.nextLong(), long256.getLong0());
                Assert.assertEquals(rnd.nextLong(), long256.getLong1());
                Assert.assertEquals(rnd.nextLong(), long256.getLong2());
                Assert.assertEquals(rnd.nextLong(), long256.getLong3());
            }
        }
    }

    @Test
    public void testLong256FullStr() {
        String expected = "0x5c504ed432cb51138bcf09aa5e8a410dd4a1e204ef84bfed1be16dfba1b22060";
        long pageSize = 128;
        Long256Impl long256 = new Long256Impl();
        Long256Impl long256a = new Long256Impl();
        try (VirtualMemory mem = new VirtualMemory(pageSize)) {

            mem.putLong256(expected);
            mem.putLong256(expected);

            mem.getLong256(0, long256);
            String actual = "0x" + Long.toHexString(long256.getLong3())
                    + Long.toHexString(long256.getLong2())
                    + Long.toHexString(long256.getLong1())
                    + Long.toHexString(long256.getLong0()
            );

            Assert.assertEquals(expected, actual);
            mem.getLong256(Long256.BYTES, long256a);

            String actual2 = "0x" + Long.toHexString(long256a.getLong3())
                    + Long.toHexString(long256a.getLong2())
                    + Long.toHexString(long256a.getLong1())
                    + Long.toHexString(long256a.getLong0()
            );
            Assert.assertEquals(expected, actual2);
        }
    }

    @Test
    public void testLong256PartialStr() {
        String expected = "0x5c504ed432cb51138bcf09aa5e8a410dd4a1e204ef84bfed";
        long pageSize = 128;
        Long256Impl long256 = new Long256Impl();
        Long256Impl long256a = new Long256Impl();
        try (VirtualMemory mem = new VirtualMemory(pageSize)) {
            mem.putLong256(expected);
            mem.putLong256(expected);
            mem.getLong256(0, long256);

            String actual = "0x";
            if (long256.getLong3() != 0) {
                actual += Long.toHexString(long256.getLong3());
            }
            if (long256.getLong2() != 0) {
                actual += Long.toHexString(long256.getLong2());
            }
            if (long256.getLong1() != 0) {
                actual += Long.toHexString(long256.getLong1());
            }
            if (long256.getLong0() != 0) {
                actual += Long.toHexString(long256.getLong0());
            }

            Assert.assertEquals(expected, actual);
            mem.getLong256(Long256.BYTES, long256a);

            String actual2 = "0x";
            if (long256a.getLong3() != 0) {
                actual2 += Long.toHexString(long256a.getLong3());
            }
            if (long256a.getLong2() != 0) {
                actual2 += Long.toHexString(long256a.getLong2());
            }
            if (long256a.getLong1() != 0) {
                actual2 += Long.toHexString(long256a.getLong1());
            }
            if (long256a.getLong0() != 0) {
                actual2 += Long.toHexString(long256a.getLong0());
            }

            Assert.assertEquals(expected, actual2);
        }
    }

    @Test
    public void testLong256DirectExternallySequenced() {
        long pageSize = 64;
        Rnd rnd = new Rnd();
        Long256Impl sink = new Long256Impl();
        try (VirtualMemory mem = new VirtualMemory(pageSize)) {
            long offset = 0;
            for (int i = 0; i < 1000; i++) {
                mem.putLong256(offset, rnd.nextLong(), rnd.nextLong(), rnd.nextLong(), rnd.nextLong());
                offset += Long256.BYTES;
            }

            rnd.reset();
            offset = 0;

            for (int i = 0; i < 1000; i++) {
                mem.getLong256(offset, sink);
                offset += Long256.BYTES;
                Assert.assertEquals(rnd.nextLong(), sink.getLong0());
                Assert.assertEquals(rnd.nextLong(), sink.getLong1());
                Assert.assertEquals(rnd.nextLong(), sink.getLong2());
                Assert.assertEquals(rnd.nextLong(), sink.getLong3());
            }
        }
    }

    @Test
    public void testDoubleCompatibility() {
        long pageSize = 64;
        try (VirtualMemory mem = new VirtualMemory(pageSize)) {
            mem.putInt(10);
            mem.putDouble(8980980284.22234);
            mem.putDoubleBytes(8979283749.72983477);
            assertEquals(8980980284.22234, mem.getDoubleBytes(0, 4, pageSize), 0.00001);
            assertEquals(8979283749.72983477, mem.getDouble(12), 0.00001);
        }
    }

    @Test
    public void testDoubleRnd() {
        try (VirtualMemory mem = new VirtualMemory(11)) {
            Rnd rnd = new Rnd();
            int n = 999;

            long o = 1;
            mem.putByte((byte) 1);

            for (int i = 0; i < n; i++) {
                mem.putDouble(o, rnd.nextDouble());
                o += 8;
            }

            rnd.reset();
            o = 1;
            assertEquals(1, mem.getByte(0));
            for (int i = 0; i < n; i++) {
                assertEquals(rnd.nextDouble(), mem.getDouble(o), 0.00001);
                o += 8;
            }
        }
    }

    @Test
    public void testDoubleRndCompatibility() {
        long pageSize = 64;
        try (VirtualMemory mem = new VirtualMemory(pageSize)) {
            // prime
            mem.putInt(10, 900);
            mem.putDouble(22, 8980980284.22234);
            mem.putDoubleBytes(84, 8979283749.72983477);
            assertEquals(8980980284.22234, mem.getDoubleBytes(0, 22, pageSize), 0.00001);
            assertEquals(8979283749.72983477, mem.getDouble(84), 0.00001);
        }
    }

    @Test
    public void testEvenPageSize() {
        try (VirtualMemory mem = new VirtualMemory(32)) {
            assertStrings(mem, false);
        }
    }

    @Test
    public void testFloat() {
        try (VirtualMemory mem = new VirtualMemory(11)) {
            Rnd rnd = new Rnd();
            int n = 999;

            mem.putByte((byte) 1);

            for (int i = 0; i < n; i++) {
                mem.putFloat(rnd.nextFloat());
            }

            rnd.reset();
            long o = 1;
            assertEquals(1, mem.getByte(0));
            for (int i = 0; i < n; i++) {
                assertEquals(rnd.nextFloat(), mem.getFloat(o), 0.00001f);
                o += 4;
            }
        }
    }

    @Test
    public void testFloatCompatibility() {
        long pageSize = 64;
        try (VirtualMemory mem = new VirtualMemory(pageSize)) {
            mem.putFloat(1024f);
            mem.putFloatBytes(2048f);
            assertEquals(1024f, mem.getFloatBytes(0, 0), 0.00001f);
            assertEquals(2048f, mem.getFloat(4), 0.0001f);
        }
    }

    @Test
    public void testFloatRnd() {
        try (VirtualMemory mem = new VirtualMemory(11)) {
            Rnd rnd = new Rnd();
            int n = 999;

            long o = 1;
            mem.putByte((byte) 1);

            for (int i = 0; i < n; i++) {
                mem.putFloat(o, rnd.nextFloat());
                o += 4;
            }

            rnd.reset();
            o = 1;
            assertEquals(1, mem.getByte(0));
            for (int i = 0; i < n; i++) {
                assertEquals(rnd.nextFloat(), mem.getFloat(o), 0.00001f);
                o += 4;
            }
        }
    }

    @Test
    public void testFloatRndCompatibility() {
        long pageSize = 64;
        try (VirtualMemory mem = new VirtualMemory(pageSize)) {
            // prime
            mem.putByte(10, (byte) 5);
            mem.putFloat(61, 1024f);
            mem.putFloatBytes(99, 2048f);
            assertEquals(1024f, mem.getFloatBytes(0, 61), 0.00001f);
            assertEquals(2048f, mem.getFloat(99), 0.0001f);
        }
    }

    @Test
    public void testInt() {
        try (VirtualMemory mem = new VirtualMemory(7)) {
            mem.putByte((byte) 1);
            int n = 999;
            for (int i = n; i > 0; i--) {
                mem.putInt(i);
            }

            long o = 1;
            assertEquals(1, mem.getByte(0));

            for (int i = n; i > 0; i--) {
                assertEquals(i, mem.getInt(o));
                o += 4;
            }
        }
    }

    @Test
    public void testIntCompatibility() {
        long pageSize = 64;
        try (VirtualMemory mem = new VirtualMemory(pageSize)) {
            mem.putInt(1024);
            mem.putIntBytes(2048);
            assertEquals(1024, mem.getIntBytes(0, 0));
            assertEquals(2048, mem.getInt(4));
        }
    }

    @Test
    public void testIntRnd() {
        try (VirtualMemory mem = new VirtualMemory(7)) {
            long o = 1;
            mem.putByte(0, (byte) 1);
            int n = 999;
            for (int i = n; i > 0; i--) {
                mem.putInt(o, i);
                o += 4;
            }

            o = 1;
            assertEquals(1, mem.getByte(0));

            for (int i = n; i > 0; i--) {
                assertEquals(i, mem.getInt(o));
                o += 4;
            }
        }
    }

    @Test
    public void testIntRndCompatibility() {
        long pageSize = 64;
        try (VirtualMemory mem = new VirtualMemory(pageSize)) {
            // prime page
            mem.putByte(10, (byte) 22);
            mem.putInt(15, 1024);
            mem.putIntBytes(55, 2048);
            assertEquals(1024, mem.getIntBytes(0, 15));
            assertEquals(2048, mem.getInt(55));
        }
    }

    @Test
    public void testJumpTo() {
        try (VirtualMemory mem = new VirtualMemory(11)) {
            mem.putByte((byte) 1);
            int n = 999;
            for (int i = n; i > 0; i--) {
                mem.putLong(i);
            }

            assertEquals(1, mem.getByte(0));

            mem.jumpTo(1);
            for (int i = n; i > 0; i--) {
                mem.putLong(n - i);
            }

            long o = 1;
            for (int i = n; i > 0; i--) {
                assertEquals(n - i, mem.getLong(o));
                o += 8;
            }
        }
    }

    @Test
    public void testJumpTo2() {
        try (VirtualMemory mem = new VirtualMemory(11)) {
            mem.jumpTo(8);
            int n = 999;
            for (int i = n; i > 0; i--) {
                mem.putLong(i);
            }
            long o = 8;
            for (int i = n; i > 0; i--) {
                assertEquals(i, mem.getLong(o));
                o += 8;
            }

        }
    }

    @Test
    public void testJumpTo3() {
        try (VirtualMemory mem = new VirtualMemory(11)) {
            mem.jumpTo(256);
            int n = 999;
            for (int i = n; i > 0; i--) {
                mem.putLong(i);
            }
            long o = 256;
            for (int i = n; i > 0; i--) {
                assertEquals(i, mem.getLong(o));
                o += 8;
            }

            mem.jumpTo(0);
            mem.jumpTo(5);
            mem.jumpTo(0);
            for (int i = n; i > 0; i--) {
                mem.putLong(i);
            }

            o = 0;
            for (int i = n; i > 0; i--) {
                assertEquals(i, mem.getLong(o));
                o += 8;
            }

        }
    }

    @Test
    public void testLongCompatibility() {
        long pageSize = 64;
        try (VirtualMemory mem = new VirtualMemory(pageSize)) {
            mem.putLong(8980980284302834L);
            mem.putLongBytes(897928374972983477L);
            assertEquals(8980980284302834L, mem.getLongBytes(0, 0, pageSize));
            assertEquals(897928374972983477L, mem.getLong(8));
        }
    }

    @Test
    public void testLongEven() {
        try (VirtualMemory mem = new VirtualMemory(11)) {
            int n = 999;
            for (int i = n; i > 0; i--) {
                mem.putLong(i);
            }

            long o = 0;
            for (int i = n; i > 0; i--) {
                assertEquals(i, mem.getLong(o));
                o += 8;
            }
        }
    }

    @Test
    public void testLongOdd() {
        try (VirtualMemory mem = new VirtualMemory(11)) {
            mem.putByte((byte) 1);
            int n = 999;
            for (int i = n; i > 0; i--) {
                mem.putLong(i);
            }

            long o = 1;
            assertEquals(1, mem.getByte(0));

            for (int i = n; i > 0; i--) {
                assertEquals(i, mem.getLong(o));
                o += 8;
            }
        }
    }

    @Test
    public void testLongRndCompatibility() {
        long pageSize = 64;
        try (VirtualMemory mem = new VirtualMemory(pageSize)) {
            mem.putLong(33, 8980980284302834L);
            mem.putLongBytes(12, 897928374972983477L);
            assertEquals(8980980284302834L, mem.getLongBytes(0, 33, pageSize));
            assertEquals(897928374972983477L, mem.getLong(12));
        }
    }

    @Test
    public void testLongRndEven() {
        try (VirtualMemory mem = new VirtualMemory(11)) {
            int n = 999;
            long o = 0;
            for (int i = n; i > 0; i--) {
                mem.putLong(o, i);
                o += 8;
            }

            o = 0;
            for (int i = n; i > 0; i--) {
                assertEquals(i, mem.getLong(o));
                o += 8;
            }
        }
    }

    @Test
    public void testLongRndOdd() {
        try (VirtualMemory mem = new VirtualMemory(11)) {
            mem.putByte(0, (byte) 1);
            int n = 999;
            long o = 1;
            for (int i = n; i > 0; i--) {
                mem.putLong(o, i);
                o += 8;
            }

            o = 1;
            assertEquals(1, mem.getByte(0));

            for (int i = n; i > 0; i--) {
                assertEquals(i, mem.getLong(o));
                o += 8;
            }
        }
    }

    @Test
    public void testNullBin() {
        try (VirtualMemory mem = new VirtualMemory(1024)) {
            final TestBinarySequence binarySequence = new TestBinarySequence();
            final byte[] buf = new byte[0];
            binarySequence.of(buf);
            mem.putBin(null);
            mem.putBin(0, 0);
            mem.putBin(binarySequence);
            long o1 = mem.putNullBin();

            assertNull(mem.getBin(0));
            assertNull(mem.getBin(8));
            BinarySequence bsview = mem.getBin(16);
            assertNotNull(bsview);
            assertEquals(0, bsview.length());
            assertNull(mem.getBin(o1));
        }
    }

    @Test
    public void testOffPageSize() {
        try (VirtualMemory mem = new VirtualMemory(12)) {
            assertStrings(mem, true);
        }
    }

    @Test
    public void testOkSize() {
        try (VirtualMemory mem = new VirtualMemory(1024)) {
            assertStrings(mem, false);
        }
    }

    @Test
    public void testShort() {
        try (VirtualMemory mem = new VirtualMemory(7)) {
            mem.putByte((byte) 1);
            short n = 999;
            for (short i = n; i > 0; i--) {
                mem.putShort(i);
            }

            long o = 1;
            assertEquals(1, mem.getByte(0));

            for (short i = n; i > 0; i--) {
                assertEquals(i, mem.getShort(o));
                o += 2;
            }
        }
    }

    @Test
    public void testShortCompatibility() {
        long pageSize = 64;
        try (VirtualMemory mem = new VirtualMemory(pageSize)) {
            mem.putShort((short) 1024);
            mem.putShortBytes((short) 2048);
            assertEquals(1024, mem.getShortBytes(0, 0, pageSize));
            assertEquals(2048, mem.getShort(2));
        }
    }

    @Test
    public void testShortRnd() {
        try (VirtualMemory mem = new VirtualMemory(7)) {
            long o = 1;
            mem.putByte(0, (byte) 1);
            short n = 999;
            for (short i = n; i > 0; i--) {
                mem.putShort(o, i);
                o += 2;
            }

            assertEquals(1, mem.getByte(0));

            o = 1;
            for (short i = n; i > 0; i--) {
                assertEquals(i, mem.getShort(o));
                o += 2;
            }
        }
    }

    @Test
    public void testShortRndCompatibility() {
        long pageSize = 64;
        try (VirtualMemory mem = new VirtualMemory(pageSize)) {
            // prime the page
            mem.putShort(5, (short) 3);
            mem.putShort(11, (short) 1024);
            mem.putShortBytes(33, (short) 2048);
            assertEquals(1024, mem.getShortBytes(0, 11, pageSize));
            assertEquals(2048, mem.getShort(33));
        }
    }

    @Test
    public void testSkip() {
        try (VirtualMemory mem = new VirtualMemory(11)) {
            mem.putByte((byte) 1);
            int n = 999;
            for (int i = n; i > 0; i--) {
                mem.putLong(i);
                mem.skip(3);
            }

            long o = 1;
            assertEquals(1, mem.getByte(0));

            for (int i = n; i > 0; i--) {
                assertEquals(i, mem.getLong(o));
                o += 11;
            }
            assertEquals(10990, mem.getAppendOffset());
        }
    }

    @Test
    public void testSmallEven() {
        try (VirtualMemory mem = new VirtualMemory(2)) {
            assertStrings(mem, false);
        }
    }

    @Test
    public void testSmallOdd() {
        try (VirtualMemory mem = new VirtualMemory(2)) {
            assertStrings(mem, true);
        }
    }

    @Test
    public void testStrRndEven() {
        testStrRnd(0, 4);
    }

    @Test
    public void testStrRndLargePage() {
        testStrRnd(1, 16);
    }

    @Test
    public void testStrRndOdd() {
        testStrRnd(1, 4);
    }

    @Test
    public void testStringStorageDimensions() {
        assertEquals(10, VirtualMemory.getStorageLength("xyz"));
        assertEquals(4, VirtualMemory.getStorageLength(""));
        assertEquals(4, VirtualMemory.getStorageLength(null));
    }

    private void assertStrings(VirtualMemory mem, boolean b) {
        if (b) {
            mem.putByte((byte) 1);
        }

        long o1 = mem.putStr("123");
        long o2 = mem.putStr("0987654321abcd");
        long o3 = mem.putStr(null);
        long o4 = mem.putStr("xyz123");
        long o5 = mem.putNullStr();
        long o6 = mem.putStr("123ohh4", 3, 3);
        long o7 = mem.putStr(null, 0, 2);

        if (b) {
            assertEquals(1, mem.getByte(0));
        }

        TestUtils.assertEquals("123", mem.getStr(o1));
        assertEquals(3, mem.getStrLen(o1));
        TestUtils.assertEquals("123", mem.getStr2(o1));

        String expected = "0987654321abcd";
        TestUtils.assertEquals("0987654321abcd", mem.getStr(o2));
        TestUtils.assertEquals("0987654321abcd", mem.getStr2(o2));

        for (int i = 0; i < expected.length(); i++) {
            long offset = o2 + 4 + i * 2;
            int page = mem.pageIndex(offset);
            long pageOffset = mem.offsetInPage(offset);
            final long pageSize = mem.getPageSize(page);
            assertEquals(expected.charAt(i), mem.getCharBytes(page, pageOffset, pageSize));
        }

        assertNull(mem.getStr(o3));
        assertNull(mem.getStr2(o3));
        TestUtils.assertEquals("xyz123", mem.getStr(o4));
        TestUtils.assertEquals("xyz123", mem.getStr2(o4));
        assertNull(mem.getStr(o5));
        assertNull(mem.getStr2(o5));
        assertEquals(-1, mem.getStrLen(o5));

        TestUtils.assertEquals("ohh", mem.getStr(o6));
        assertNull(mem.getStr(o7));

        CharSequence s1 = mem.getStr(o1);
        CharSequence s2 = mem.getStr2(o2);
        assertFalse(Chars.equals(s1, s2));
    }

    private void testBinSequence0(long mem1Size, long mem2Size) {
        Rnd rnd = new Rnd();
        int n = 999;

        final TestBinarySequence binarySequence = new TestBinarySequence();
        final byte[] buffer = new byte[600];
        binarySequence.of(buffer);

        try (VirtualMemory mem = new VirtualMemory(mem1Size)) {
            for (int i = 0; i < n; i++) {
                if (rnd.nextPositiveInt() % 16 == 0) {
                    mem.putBin(null);
                    continue;
                }

                int sz = buffer.length;
                for (int j = 0; j < sz; j++) {
                    buffer[j] = rnd.nextByte();
                }

                mem.putBin(binarySequence);
            }

            try (VirtualMemory mem2 = new VirtualMemory(mem2Size)) {
                long o = 0L;
                for (int i = 0; i < n; i++) {
                    BinarySequence sequence = mem.getBin(o);
                    if (sequence == null) {
                        o += 8;
                    } else {
                        o += sequence.length() + 8;
                    }

                    mem2.putBin(sequence);
                }

                o = 0;

                // compare
                for (int i = 0; i < n; i++) {
                    BinarySequence sequence1 = mem.getBin(o);
                    BinarySequence sequence2 = mem2.getBin(o);

                    if (sequence1 == null) {
                        assertNull(sequence2);
                        assertEquals(TableUtils.NULL_LEN, mem2.getBinLen(o));
                        o += 8;
                    } else {
                        assertNotNull(sequence2);
                        assertEquals(mem.getBinLen(o), mem2.getBinLen(o));
                        assertEquals(sequence1.length(), sequence2.length());
                        for (long l = 0, len = sequence1.length(); l < len; l++) {
                            assertEquals(sequence1.byteAt(l), sequence2.byteAt(l));
                        }

                        o += sequence1.length() + 8;
                    }
                }
            }
        }
    }

    private void testStrRnd(long offset, long pageSize) {
        Rnd rnd = new Rnd();
        int N = 1000;
        final int M = 4;
        try (VirtualMemory mem = new VirtualMemory(pageSize)) {
            long o = offset;
            for (int i = 0; i < N; i++) {
                int flag = rnd.nextInt();
                if ((flag % 4) == 0) {
                    mem.putStr(o, null);
                    o += 4;
                } else if ((flag % 2) == 0) {
                    mem.putStr(o, "");
                    o += 4;
                } else {
                    mem.putStr(o, rnd.nextChars(M));
                    o += M * 2 + 4;
                }
            }

            rnd.reset();
            o = offset;
            for (int i = 0; i < N; i++) {
                int flag = rnd.nextInt();
                if ((flag % 4) == 0) {
                    assertNull(mem.getStr(o));
                    o += 4;
                } else if ((flag % 2) == 0) {
                    TestUtils.assertEquals("", mem.getStr(o));
                    o += 4;
                } else {
                    TestUtils.assertEquals(rnd.nextChars(M), mem.getStr(o));
                    o += M * 2 + 4;
                }
            }
        }
    }
}