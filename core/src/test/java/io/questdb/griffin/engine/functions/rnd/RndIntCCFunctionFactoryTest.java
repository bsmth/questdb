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
import io.questdb.std.Numbers;
import org.junit.Assert;
import org.junit.Test;

public class RndIntCCFunctionFactoryTest extends AbstractFunctionFactoryTest {
    @Test
    public void testInvalidRange() {
        assertFailure(0, "invalid range", 20, 10, 0);
        assertFailure(0, "invalid range", 5, 5, 0);
        assertFailure(12, "invalid NaN rate", 1, 4, -1);
    }

    @Test
    public void testNegativeRange() throws SqlException {
        assertValues(-134, -40, 4);
    }

    @Test
    public void testNoNaNs() throws SqlException {
        assertValues(10, 20, 0);
    }

    @Test
    public void testPositiveRange() throws SqlException {
        assertValues(10, 20, 5);
    }

    @Override
    protected void addExtraFunctions() {
        functions.add(new NegIntFunctionFactory());
    }

    @Override
    protected FunctionFactory getFunctionFactory() {
        return new RndIntCCFunctionFactory();
    }

    private void assertFunctionValues(int lo, int hi, boolean expectNan, Invocation invocation, Function function1) {
        int nanCount = 0;
        for (int i = 0; i < 1000; i++) {
            int value = function1.getInt(invocation.getRecord());
            if (value == Numbers.INT_NaN) {
                nanCount++;
            } else {
                Assert.assertTrue(value <= hi && value >= lo);
            }
        }
        if (expectNan) {
            Assert.assertTrue(nanCount > 0);
        } else {
            Assert.assertEquals(0, nanCount);
        }
    }

    private void assertValues(int lo, int hi, int nanRate) throws SqlException {
        Invocation invocation = call(lo, hi, nanRate);
        assertFunctionValues(lo, hi, nanRate > 0, invocation, invocation.getFunction1());
        assertFunctionValues(lo, hi, nanRate > 0, invocation, invocation.getFunction2());
    }
}