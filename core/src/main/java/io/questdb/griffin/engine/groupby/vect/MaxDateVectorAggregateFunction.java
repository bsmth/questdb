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

package io.questdb.griffin.engine.groupby.vect;

import io.questdb.cairo.ArrayColumnTypes;
import io.questdb.cairo.ColumnType;
import io.questdb.cairo.sql.Record;
import io.questdb.griffin.engine.functions.DateFunction;
import io.questdb.std.Rosti;
import io.questdb.std.Unsafe;
import io.questdb.std.Vect;

import java.util.concurrent.atomic.LongAccumulator;
import java.util.function.LongBinaryOperator;

public class MaxDateVectorAggregateFunction extends DateFunction implements VectorAggregateFunction {

    public static final LongBinaryOperator MAX = Math::max;
    private final LongAccumulator max = new LongAccumulator(
            MAX, Long.MIN_VALUE
    );
    private final int columnIndex;
    private int valueOffset;

    public MaxDateVectorAggregateFunction(int position, int columnIndex) {
        super(position);
        this.columnIndex = columnIndex;
    }

    @Override
    public void pushValueTypes(ArrayColumnTypes types) {
        this.valueOffset = types.getColumnCount();
        types.add(ColumnType.LONG);
    }

    @Override
    public int getValueOffset() {
        return valueOffset;
    }

    @Override
    public void initRosti(long pRosti) {
        Unsafe.getUnsafe().putLong(Rosti.getInitialValueSlot(pRosti, valueOffset), Long.MIN_VALUE);
    }

    @Override
    public void aggregate(long pRosti, long keyAddress, long valueAddress, long count, int workerId) {
        if (valueAddress == 0) {
            Rosti.keyedIntDistinct(pRosti, keyAddress, count);
        } else {
            Rosti.keyedIntMaxLong(pRosti, keyAddress, valueAddress, count, valueOffset);
        }
    }

    @Override
    public void merge(long pRostiA, long pRostiB) {
        Rosti.keyedIntMaxLongMerge(pRostiA, pRostiB, valueOffset);
    }

    @Override
    public void wrapUp(long pRosti) {
        Rosti.keyedIntMaxLongWrapUp(pRosti, valueOffset, max.longValue());
    }

    @Override
    public void aggregate(long address, long count, int workerId) {
        if (address != 0) {
            max.accumulate(Vect.maxLong(address, count));
        }
    }

    @Override
    public int getColumnIndex() {
        return columnIndex;
    }

    @Override
    public void clear() {
        max.reset();
    }

    @Override
    public long getDate(Record rec) {
        return max.longValue();
    }
}
