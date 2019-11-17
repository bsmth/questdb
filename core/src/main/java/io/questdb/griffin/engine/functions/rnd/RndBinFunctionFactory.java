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
import io.questdb.griffin.engine.functions.BinFunction;
import io.questdb.griffin.engine.functions.StatelessFunction;
import io.questdb.std.BinarySequence;
import io.questdb.std.ObjList;
import io.questdb.std.Rnd;

public class RndBinFunctionFactory implements FunctionFactory {
    @Override
    public String getSignature() {
        return "rnd_bin()";
    }

    @Override
    public Function newInstance(ObjList<Function> args, int position, CairoConfiguration configuration) {
        return new FixLenFunction(position, configuration);
    }

    private static final class FixLenFunction extends BinFunction implements StatelessFunction {
        private final Sequence sequence = new Sequence();

        public FixLenFunction(int position, CairoConfiguration configuration) {
            super(position);
            this.sequence.rnd = SharedRandom.getRandom(configuration);
            this.sequence.len = 32;
        }

        @Override
        public BinarySequence getBin(Record rec) {
            return sequence;
        }

        @Override
        public long getBinLen(Record rec) {
            return sequence.len;
        }
    }

    private static class Sequence implements BinarySequence {
        private Rnd rnd;
        private long len;

        @Override
        public byte byteAt(long index) {
            return rnd.nextByte();
        }

        @Override
        public long length() {
            return len;
        }
    }
}
