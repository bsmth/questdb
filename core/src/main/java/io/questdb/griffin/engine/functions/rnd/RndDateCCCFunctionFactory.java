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

package io.questdb.griffin.engine.functions.rnd;

import io.questdb.cairo.CairoConfiguration;
import io.questdb.cairo.sql.Function;
import io.questdb.cairo.sql.Record;
import io.questdb.griffin.FunctionFactory;
import io.questdb.griffin.SqlException;
import io.questdb.griffin.engine.functions.DateFunction;
import io.questdb.griffin.engine.functions.StatelessFunction;
import io.questdb.std.Numbers;
import io.questdb.std.ObjList;
import io.questdb.std.Rnd;

public class RndDateCCCFunctionFactory implements FunctionFactory {
    @Override
    public String getSignature() {
        return "rnd_date(mmi)";
    }

    @Override
    public Function newInstance(ObjList<Function> args, int position, CairoConfiguration configuration) throws SqlException {
        final long lo = args.getQuick(0).getDate(null);
        final long hi = args.getQuick(1).getDate(null);
        final int nanRate = args.getQuick(2).getInt(null);

        if (nanRate < 0) {
            throw SqlException.$(args.getQuick(2).getPosition(), "invalid NaN rate");
        }

        if (lo < hi) {
            return new Func(position, lo, hi, nanRate, configuration);
        }

        throw SqlException.$(position, "invalid range");
    }

    private static class Func extends DateFunction implements StatelessFunction {
        private final long lo;
        private final long range;
        private final int nanRate;
        private final Rnd rnd;

        public Func(int position, long lo, long hi, int nanRate, CairoConfiguration configuration) {
            super(position);
            this.lo = lo;
            this.range = hi - lo + 1;
            this.nanRate = nanRate + 1;
            this.rnd = SharedRandom.getRandom(configuration);
        }

        @Override
        public long getDate(Record rec) {
            if ((rnd.nextInt() % nanRate) == 1) {
                return Numbers.LONG_NaN;
            }
            return lo + rnd.nextPositiveLong() % range;
        }
    }
}
