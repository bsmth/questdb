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

package io.questdb.griffin.engine.functions.rnd;

import io.questdb.cairo.sql.Function;
import io.questdb.griffin.FunctionFactory;
import io.questdb.griffin.SqlException;
import io.questdb.griffin.engine.AbstractFunctionFactoryTest;
import io.questdb.griffin.engine.functions.math.NegIntFunctionFactory;
import io.questdb.std.ObjHashSet;
import org.junit.Assert;
import org.junit.Test;

public class RndSymbolFunctionFactoryTest extends AbstractFunctionFactoryTest {
    @Test
    public void testFixedLength() throws SqlException {
        int symbolCount = 10;
        int symbolLen = 4;
        int nullRate = 2;
        Invocation invocation = call(symbolCount, symbolLen, symbolLen, nullRate);
        assertFixLen(symbolCount, symbolLen, nullRate, invocation.getFunction1());
        assertFixLen(symbolCount, symbolLen, nullRate, invocation.getFunction2());
    }

    @Test
    public void testFixedLengthNoNull() throws SqlException {
        int symbolCount = 10;
        int symbolLen = 4;
        int nullRate = 0;
        Invocation invocation = call(symbolCount, symbolLen, symbolLen, nullRate);
        assertFixLen(symbolCount, symbolLen, nullRate, invocation.getFunction1());
        assertFixLen(symbolCount, symbolLen, nullRate, invocation.getFunction2());
    }

    @Test
    public void testInvalidNullRate() {
        assertFailure(18, "rate must be positive", 10, 1, 2, -5);
    }

    @Test
    public void testInvalidRange() {
        assertFailure(0, "invalid range", 10, 8, 6, 2);
    }

    @Test
    public void testNegativeLen() {
        assertFailure(0, "invalid range", 10, -8, -6, 2);
    }

    @Test
    public void testPositiveCount() {
        assertFailure(11, "invalid symbol count", 0, 4, 6, 2);
    }

    @Test
    public void testVarLength() throws SqlException {
        int symbolCount = 10;
        int symbolMin = 4;
        int symbolMax = 6;
        int nullRate = 2;
        Invocation invocation = call(symbolCount, symbolMin, symbolMax, nullRate);
        assertVarLen(symbolCount, symbolMin, symbolMax, nullRate, invocation.getFunction1());
        assertVarLen(symbolCount, symbolMin, symbolMax, nullRate, invocation.getFunction2());
    }

    @Test
    public void testVarLengthNoNull() throws SqlException {
        int symbolCount = 10;
        int symbolMin = 4;
        int symbolMax = 6;
        int nullRate = 0;
        Invocation invocation = call(symbolCount, symbolMin, symbolMax, nullRate);
        assertVarLen(symbolCount, symbolMin, symbolMax, nullRate, invocation.getFunction1());
        assertVarLen(symbolCount, symbolMin, symbolMax, nullRate, invocation.getFunction2());
    }

    @Override
    protected void addExtraFunctions() {
        functions.add(new NegIntFunctionFactory());
    }

    @Override
    protected FunctionFactory getFunctionFactory() {
        return new RndSymbolFunctionFactory();
    }

    private void assertFixLen(int symbolCount, int symbolLen, int nullRate, Function function) {
        ObjHashSet<CharSequence> set = new ObjHashSet<>();
        int nullCount = 0;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < 100; i++) {
            CharSequence value = function.getSymbol(null);
            if (value == null) {
                nullCount++;
            } else {
                if (value.length() < min) {
                    min = value.length();
                }

                if (value.length() > max) {
                    max = value.length();
                }
                set.add(value);
            }
        }

        if (nullRate > 0) {
            Assert.assertTrue(nullCount > 0);
        } else {
            Assert.assertEquals(0, nullCount);
        }
        Assert.assertTrue(set.size() <= symbolCount);
        Assert.assertEquals(symbolLen, max);
        Assert.assertEquals(symbolLen, min);
    }

    private void assertVarLen(int symbolCount, int symbolMin, int symbolMax, int nullRate, Function function) {
        ObjHashSet<CharSequence> set = new ObjHashSet<>();
        int nullCount = 0;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < 100; i++) {
            CharSequence value = function.getSymbol(null);
            if (value == null) {
                nullCount++;
            } else {
                if (value.length() < min) {
                    min = value.length();
                }

                if (value.length() > max) {
                    max = value.length();
                }
                set.add(value);
            }
        }

        if (nullRate > 0) {
            Assert.assertTrue(nullCount > 0);
        } else {
            Assert.assertEquals(0, nullCount);
        }
        Assert.assertTrue(set.size() <= symbolCount);
        Assert.assertTrue(symbolMin <= min);
        Assert.assertTrue(symbolMax >= max);
        Assert.assertNotEquals(min, max);
    }
}