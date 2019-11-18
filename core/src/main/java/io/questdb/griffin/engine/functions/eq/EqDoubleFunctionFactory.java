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

package io.questdb.griffin.engine.functions.eq;

import io.questdb.cairo.CairoConfiguration;
import io.questdb.cairo.ColumnType;
import io.questdb.cairo.sql.Function;
import io.questdb.cairo.sql.Record;
import io.questdb.griffin.FunctionFactory;
import io.questdb.griffin.engine.functions.BinaryFunction;
import io.questdb.griffin.engine.functions.BooleanFunction;
import io.questdb.griffin.engine.functions.UnaryFunction;
import io.questdb.std.Numbers;
import io.questdb.std.ObjList;

public class EqDoubleFunctionFactory implements FunctionFactory {
    @Override
    public String getSignature() {
        return "=(DD)";
    }

    @Override
    public Function newInstance(ObjList<Function> args, int position, CairoConfiguration configuration) {
        // this is probably a special case factory
        // NaN is always a double, so this could lead comparisons of all primitive types
        // to NaN route to this factory. Obviously comparing naively will not work
        // We have to check arg types and when NaN is present we would generate special case
        // functions for NaN checks.

        Function left = args.getQuick(0);
        Function right = args.getQuick(1);

        if (left.isConstant() && left.getType() == ColumnType.DOUBLE && Double.isNaN(left.getDouble(null))) {
            switch (right.getType()) {
                case ColumnType.INT:
                    return new FuncIntIsNaN(position, right);
                case ColumnType.LONG:
                    return new FuncLongIsNaN(position, right);
                case ColumnType.DATE:
                    return new FuncDateIsNaN(position, right);
                case ColumnType.TIMESTAMP:
                    return new FuncTimestampIsNaN(position, right);
                case ColumnType.FLOAT:
                    return new FuncFloatIsNaN(position, right);
                default:
                    // double
                    return new FuncDoubleIsNaN(position, right);
            }
        } else if (right.isConstant() && right.getType() == ColumnType.DOUBLE && Double.isNaN(right.getDouble(null))) {
            switch (left.getType()) {
                case ColumnType.INT:
                    return new FuncIntIsNaN(position, left);
                case ColumnType.LONG:
                    return new FuncLongIsNaN(position, left);
                case ColumnType.DATE:
                    return new FuncDateIsNaN(position, left);
                case ColumnType.TIMESTAMP:
                    return new FuncTimestampIsNaN(position, left);
                case ColumnType.FLOAT:
                    return new FuncFloatIsNaN(position, left);
                default:
                    // double
                    return new FuncDoubleIsNaN(position, left);
            }
        }
        return new Func(position, args.getQuick(0), args.getQuick(1));
    }

    private static class Func extends BooleanFunction implements BinaryFunction {
        private final Function left;
        private final Function right;

        public Func(int position, Function left, Function right) {
            super(position);
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean getBool(Record rec) {
            final double l = left.getDouble(rec);
            final double r = right.getDouble(rec);
            return l != l && r != r || Math.abs(l - r) < 0.0000000001;
        }

        @Override
        public Function getLeft() {
            return left;
        }

        @Override
        public Function getRight() {
            return right;
        }
    }

    private static class FuncIntIsNaN extends BooleanFunction implements UnaryFunction {
        private final Function arg;

        public FuncIntIsNaN(int position, Function arg) {
            super(position);
            this.arg = arg;
        }

        @Override
        public boolean getBool(Record rec) {
            return arg.getInt(rec) == Numbers.INT_NaN;
        }

        @Override
        public Function getArg() {
            return arg;
        }
    }

    private static class FuncLongIsNaN extends BooleanFunction implements UnaryFunction {
        private final Function arg;

        public FuncLongIsNaN(int position, Function arg) {
            super(position);
            this.arg = arg;
        }

        @Override
        public boolean getBool(Record rec) {
            return arg.getLong(rec) == Numbers.LONG_NaN;
        }

        @Override
        public Function getArg() {
            return arg;
        }
    }

    private static class FuncDateIsNaN extends BooleanFunction implements UnaryFunction {
        private final Function arg;

        public FuncDateIsNaN(int position, Function arg) {
            super(position);
            this.arg = arg;
        }

        @Override
        public boolean getBool(Record rec) {
            return arg.getDate(rec) == Numbers.LONG_NaN;
        }

        @Override
        public Function getArg() {
            return arg;
        }
    }

    private static class FuncTimestampIsNaN extends BooleanFunction implements UnaryFunction {
        private final Function arg;

        public FuncTimestampIsNaN(int position, Function arg) {
            super(position);
            this.arg = arg;
        }

        @Override
        public boolean getBool(Record rec) {
            return arg.getTimestamp(rec) == Numbers.LONG_NaN;
        }

        @Override
        public Function getArg() {
            return arg;
        }
    }

    private static class FuncFloatIsNaN extends BooleanFunction implements UnaryFunction {
        private final Function arg;

        public FuncFloatIsNaN(int position, Function arg) {
            super(position);
            this.arg = arg;
        }

        @Override
        public boolean getBool(Record rec) {
            return Float.isNaN(arg.getFloat(rec));
        }

        @Override
        public Function getArg() {
            return arg;
        }
    }

    private static class FuncDoubleIsNaN extends BooleanFunction implements UnaryFunction {
        private final Function arg;

        public FuncDoubleIsNaN(int position, Function arg) {
            super(position);
            this.arg = arg;
        }

        @Override
        public boolean getBool(Record rec) {
            return Double.isNaN(arg.getDouble(rec));
        }

        @Override
        public Function getArg() {
            return arg;
        }
    }
}
