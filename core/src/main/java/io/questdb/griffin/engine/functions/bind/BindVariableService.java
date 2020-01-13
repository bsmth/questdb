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

package io.questdb.griffin.engine.functions.bind;

import io.questdb.cairo.ColumnType;
import io.questdb.cairo.sql.Function;
import io.questdb.std.*;

public class BindVariableService {
    private final CharSequenceObjHashMap<Function> namedVariables = new CharSequenceObjHashMap<>();
    private final ObjList<Function> indexedVariables = new ObjList<>();

    public void clear() {
        namedVariables.clear();
        indexedVariables.clear();
    }

    public int getIndexedVariableCount() {
        return indexedVariables.size();
    }

    public Function getFunction(CharSequence name) {
        assert name != null;
        assert Chars.startsWith(name, ':');
        return namedVariables.valueAt(namedVariables.keyIndex(name, 1, name.length()));
    }

    public Function getFunction(int index) {
        final int n = indexedVariables.size();
        if (index < n) {
            return indexedVariables.getQuick(index);
        }
        return null;
    }

    public void setBin(CharSequence name, BinarySequence value) {
        int index = namedVariables.keyIndex(name);
        if (index > -1) {
            namedVariables.putAt(index, name, new BinBindVariable(value));
        } else {
            Function function = namedVariables.valueAtQuick(index);
            if (function instanceof BinBindVariable) {
                ((BinBindVariable) function).value = value;
            } else {
                throw BindException.init().put("bind variable '").put(name).put("' is already defined as ").put(ColumnType.nameOf(function.getType()));
            }
        }
    }

    public void setBin(int index, BinarySequence value) {
        if (index < indexedVariables.size()) {
            Function function = indexedVariables.getQuick(index);
            if (function == null) {
                indexedVariables.setQuick(index, new BinBindVariable(value));
            } else if (function instanceof BinBindVariable) {
                ((BinBindVariable) function).value = value;
            } else {
                throw BindException.init().put("bind variable at ").put(index).put(" is already defined as ").put(ColumnType.nameOf(function.getType()));
            }
        } else {
            indexedVariables.extendAndSet(index, new BinBindVariable(value));
        }
    }

    public void setBoolean(CharSequence name, boolean value) {
        int index = namedVariables.keyIndex(name);
        if (index > -1) {
            namedVariables.putAt(index, name, new BooleanBindVariable(value));
        } else {
            Function function = namedVariables.valueAtQuick(index);
            if (function instanceof BooleanBindVariable) {
                ((BooleanBindVariable) function).value = value;
            } else {
                throw BindException.init().put("bind variable '").put(name).put("' is already defined as ").put(ColumnType.nameOf(function.getType()));
            }
        }
    }

    public void setBoolean(int index, boolean value) {
        if (index < indexedVariables.size()) {
            Function function = indexedVariables.getQuick(index);
            if (function == null) {
                indexedVariables.setQuick(index, new BooleanBindVariable(value));
            } else if (function instanceof BooleanBindVariable) {
                ((BooleanBindVariable) function).value = value;
            } else {
                throw BindException.init().put("bind variable at ").put(index).put(" is already defined as ").put(ColumnType.nameOf(function.getType()));
            }
        } else {
            indexedVariables.extendAndSet(index, new BooleanBindVariable(value));
        }
    }

    public void setByte(CharSequence name, byte value) {
        int index = namedVariables.keyIndex(name);
        if (index > -1) {
            namedVariables.putAt(index, name, new ByteBindVariable(value));
        } else {
            Function function = namedVariables.valueAtQuick(index);
            if (function instanceof ByteBindVariable) {
                ((ByteBindVariable) function).value = value;
            } else {
                throw BindException.init().put("bind variable '").put(name).put("' is already defined as ").put(ColumnType.nameOf(function.getType()));
            }
        }
    }

    public void setByte(int index, byte value) {
        if (index < indexedVariables.size()) {
            Function function = indexedVariables.getQuick(index);
            if (function == null) {
                indexedVariables.setQuick(index, new ByteBindVariable(value));
            } else if (function instanceof ByteBindVariable) {
                ((ByteBindVariable) function).value = value;
            } else {
                throw BindException.init().put("bind variable at ").put(index).put(" is already defined as ").put(ColumnType.nameOf(function.getType()));
            }
        } else {
            indexedVariables.extendAndSet(index, new ByteBindVariable(value));
        }
    }

    public void setDate(CharSequence name, long value) {
        int index = namedVariables.keyIndex(name);
        if (index > -1) {
            namedVariables.putAt(index, name, new DateBindVariable(value));
        } else {
            Function function = namedVariables.valueAtQuick(index);
            if (function instanceof DateBindVariable) {
                ((DateBindVariable) function).value = value;
            } else {
                throw BindException.init().put("bind variable '").put(name).put("' is already defined as ").put(ColumnType.nameOf(function.getType()));
            }
        }
    }

    public void setChar(CharSequence name, char value) {
        int index = namedVariables.keyIndex(name);
        if (index > -1) {
            namedVariables.putAt(index, name, new CharBindVariable(value));
        } else {
            Function function = namedVariables.valueAtQuick(index);
            if (function instanceof CharBindVariable) {
                ((CharBindVariable) function).value = value;
            } else {
                throw BindException.init().put("bind variable '").put(name).put("' is already defined as ").put(ColumnType.nameOf(function.getType()));
            }
        }
    }

    public void setDate(int index, long value) {
        if (index < indexedVariables.size()) {
            Function function = indexedVariables.getQuick(index);
            if (function == null) {
                indexedVariables.setQuick(index, new DateBindVariable(value));
            } else if (function instanceof DateBindVariable) {
                ((DateBindVariable) function).value = value;
            } else {
                throw BindException.init().put("bind variable at ").put(index).put(" is already defined as ").put(ColumnType.nameOf(function.getType()));
            }
        } else {
            indexedVariables.extendAndSet(index, new DateBindVariable(value));
        }
    }

    public void setDouble(CharSequence name, double value) {
        int index = namedVariables.keyIndex(name);
        if (index > -1) {
            namedVariables.putAt(index, name, new DoubleBindVariable(value));
        } else {
            Function function = namedVariables.valueAtQuick(index);
            if (function instanceof DoubleBindVariable) {
                ((DoubleBindVariable) function).value = value;
            } else {
                throw BindException.init().put("bind variable '").put(name).put("' is already defined as ").put(ColumnType.nameOf(function.getType()));
            }
        }
    }

    public void setDouble(int index, double value) {
        if (index < indexedVariables.size()) {
            Function function = indexedVariables.getQuick(index);
            if (function == null) {
                indexedVariables.setQuick(index, new DoubleBindVariable(value));
            } else if (function instanceof DoubleBindVariable) {
                ((DoubleBindVariable) function).value = value;
            } else {
                throw BindException.init().put("bind variable at ").put(index).put(" is already defined as ").put(ColumnType.nameOf(function.getType()));
            }
        } else {
            indexedVariables.extendAndSet(index, new DoubleBindVariable(value));
        }
    }

    public void setFloat(CharSequence name, float value) {
        int index = namedVariables.keyIndex(name);
        if (index > -1) {
            namedVariables.putAt(index, name, new FloatBindVariable(value));
        } else {
            Function function = namedVariables.valueAtQuick(index);
            if (function instanceof FloatBindVariable) {
                ((FloatBindVariable) function).value = value;
            } else {
                throw BindException.init().put("bind variable '").put(name).put("' is already defined as ").put(ColumnType.nameOf(function.getType()));
            }
        }
    }

    public void setFloat(int index, float value) {
        if (index < indexedVariables.size()) {
            Function function = indexedVariables.getQuick(index);
            if (function == null) {
                indexedVariables.setQuick(index, new FloatBindVariable(value));
            } else if (function instanceof FloatBindVariable) {
                ((FloatBindVariable) function).value = value;
            } else {
                throw BindException.init().put("bind variable at ").put(index).put(" is already defined as ").put(ColumnType.nameOf(function.getType()));
            }
        } else {
            indexedVariables.extendAndSet(index, new FloatBindVariable(value));
        }
    }

    public void setInt(CharSequence name, int value) {
        int index = namedVariables.keyIndex(name);
        if (index > -1) {
            namedVariables.putAt(index, name, new IntBindVariable(value));
        } else {
            Function function = namedVariables.valueAtQuick(index);
            if (function instanceof IntBindVariable) {
                ((IntBindVariable) function).value = value;
            } else {
                throw BindException.init().put("bind variable '").put(name).put("' is already defined as ").put(ColumnType.nameOf(function.getType()));
            }
        }
    }

    public void setLong256Null(CharSequence name) {
        setLong256(name, Long256Impl.NULL_LONG256);
    }

    public void setLong256(CharSequence name, long l0, long l1, long l2, long l3) {
        int index = namedVariables.keyIndex(name);
        if (index > -1) {
            namedVariables.putAt(index, name, new Long256BindVariable(l0, l1, l2, l3));
        } else {
            Function function = namedVariables.valueAtQuick(index);
            if (function instanceof Long256BindVariable) {
                Long256Impl v = ((Long256BindVariable) function).value;
                v.setLong0(l0);
                v.setLong1(l1);
                v.setLong2(l2);
                v.setLong3(l3);
            } else {
                throw BindException.init().put("bind variable '").put(name).put("' is already defined as ").put(ColumnType.nameOf(function.getType()));
            }
        }
    }

    public void setLong256(CharSequence name, Long256 value) {
        int index = namedVariables.keyIndex(name);
        if (index > -1) {
            namedVariables.putAt(index, name, new Long256BindVariable(value));
        } else {
            Function function = namedVariables.valueAtQuick(index);
            if (function instanceof Long256BindVariable) {
                ((Long256BindVariable) function).value.copyFrom(value);
            } else {
                throw BindException.init().put("bind variable '").put(name).put("' is already defined as ").put(ColumnType.nameOf(function.getType()));
            }
        }
    }

    public void setInt(int index, int value) {
        if (index < indexedVariables.size()) {
            Function function = indexedVariables.getQuick(index);
            if (function == null) {
                indexedVariables.setQuick(index, new IntBindVariable(value));
            } else if (function instanceof IntBindVariable) {
                ((IntBindVariable) function).value = value;
            } else {
                throw BindException.init().put("bind variable at ").put(index).put(" is already defined as ").put(ColumnType.nameOf(function.getType()));
            }
        } else {
            indexedVariables.extendAndSet(index, new IntBindVariable(value));
        }
    }

    public void setLong256(int index, long l0, long l1, long l2, long l3) {
        if (index < indexedVariables.size()) {
            Function function = indexedVariables.getQuick(index);
            if (function == null) {
                indexedVariables.setQuick(index, new Long256BindVariable(l0, l1, l2, l3));
            } else if (function instanceof Long256BindVariable) {
                final Long256Impl v = ((Long256BindVariable) function).value;
                v.setLong0(l0);
                v.setLong1(l1);
                v.setLong2(l2);
                v.setLong3(l3);
            } else {
                throw BindException.init().put("bind variable at ").put(index).put(" is already defined as ").put(ColumnType.nameOf(function.getType()));
            }
        } else {
            indexedVariables.extendAndSet(index, new Long256BindVariable(l0, l1, l2, l3));
        }
    }

    public void setChar(int index, char value) {
        if (index < indexedVariables.size()) {
            Function function = indexedVariables.getQuick(index);
            if (function == null) {
                indexedVariables.setQuick(index, new CharBindVariable(value));
            } else if (function instanceof CharBindVariable) {
                ((CharBindVariable) function).value = value;
            } else {
                throw BindException.init().put("bind variable at ").put(index).put(" is already defined as ").put(ColumnType.nameOf(function.getType()));
            }
        } else {
            indexedVariables.extendAndSet(index, new CharBindVariable(value));
        }
    }

    public void setLong(CharSequence name, long value) {
        int index = namedVariables.keyIndex(name);
        if (index > -1) {
            namedVariables.putAt(index, name, new LongBindVariable(value));
        } else {
            Function function = namedVariables.valueAtQuick(index);
            if (function instanceof LongBindVariable) {
                ((LongBindVariable) function).value = value;
            } else {
                throw BindException.init().put("bind variable '").put(name).put("' is already defined as ").put(ColumnType.nameOf(function.getType()));
            }
        }
    }

    public void setLong(int index, long value) {
        if (index < indexedVariables.size()) {
            Function function = indexedVariables.getQuick(index);
            if (function == null) {
                indexedVariables.setQuick(index, new LongBindVariable(value));
            } else if (function instanceof LongBindVariable) {
                ((LongBindVariable) function).value = value;
            } else {
                throw BindException.init().put("bind variable at ").put(index).put(" is already defined as ").put(ColumnType.nameOf(function.getType()));
            }
        } else {
            indexedVariables.extendAndSet(index, new LongBindVariable(value));
        }
    }

    public void setShort(int index, short value) {
        if (index < indexedVariables.size()) {
            Function function = indexedVariables.getQuick(index);
            if (function == null) {
                indexedVariables.setQuick(index, new ShortBindVariable(value));
            } else if (function instanceof ShortBindVariable) {
                ((ShortBindVariable) function).value = value;
            } else {
                throw BindException.init().put("bind variable at ").put(index).put(" is already defined as ").put(ColumnType.nameOf(function.getType()));
            }
        } else {
            indexedVariables.extendAndSet(index, new ShortBindVariable(value));
        }
    }

    public void setShort(CharSequence name, short value) {
        int index = namedVariables.keyIndex(name);
        if (index > -1) {
            namedVariables.putAt(index, name, new ShortBindVariable(value));
        } else {
            Function function = namedVariables.valueAtQuick(index);
            if (function instanceof ShortBindVariable) {
                ((ShortBindVariable) function).value = value;
            } else {
                throw BindException.init().put("bind variable '").put(name).put("' is already defined as ").put(ColumnType.nameOf(function.getType()));
            }
        }
    }

    public void setStr(int index, CharSequence value) {
        if (index < indexedVariables.size()) {
            Function function = indexedVariables.getQuick(index);
            if (function == null) {
                indexedVariables.setQuick(index, new StrBindVariable(value));
            } else if (function instanceof StrBindVariable) {
                ((StrBindVariable) function).setValue(value);
            } else {
                throw BindException.init().put("bind variable at ").put(index).put(" is already defined as ").put(ColumnType.nameOf(function.getType()));
            }
        } else {
            indexedVariables.extendAndSet(index, new StrBindVariable(value));
        }
    }

    public void setStr(CharSequence name, CharSequence value) {
        int index = namedVariables.keyIndex(name);
        if (index > -1) {
            namedVariables.putAt(index, name, new StrBindVariable(value));
        } else {
            Function function = namedVariables.valueAtQuick(index);
            if (function instanceof StrBindVariable) {
                ((StrBindVariable) function).setValue(value);
            } else {
                throw BindException.init().put("bind variable '").put(name).put("' is already defined as ").put(ColumnType.nameOf(function.getType()));
            }
        }
    }

    public void setTimestamp(int index, long value) {
        if (index < indexedVariables.size()) {
            Function function = indexedVariables.getQuick(index);
            if (function == null) {
                indexedVariables.setQuick(index, new TimestampBindVariable(value));
            } else if (function instanceof TimestampBindVariable) {
                ((TimestampBindVariable) function).value = value;
            } else {
                throw BindException.init().put("bind variable at ").put(index).put(" is already defined as ").put(ColumnType.nameOf(function.getType()));
            }
        } else {
            indexedVariables.extendAndSet(index, new TimestampBindVariable(value));
        }
    }

    public void setTimestamp(CharSequence name, long value) {
        int index = namedVariables.keyIndex(name);
        if (index > -1) {
            namedVariables.putAt(index, name, new TimestampBindVariable(value));
        } else {
            Function function = namedVariables.valueAtQuick(index);
            if (function instanceof TimestampBindVariable) {
                ((TimestampBindVariable) function).value = value;
            } else {
                throw BindException.init().put("bind variable '").put(name).put("' is already defined as ").put(ColumnType.nameOf(function.getType()));
            }
        }
    }
}
