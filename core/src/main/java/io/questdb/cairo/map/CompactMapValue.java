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

package io.questdb.cairo.map;

import io.questdb.cairo.VirtualMemory;

class CompactMapValue implements MapValue {

    private final VirtualMemory entries;
    private final long[] columnOffsets;
    private long currentValueOffset;
    private boolean _new;
    private CompactMapRecord record;

    CompactMapValue(VirtualMemory entries, long[] columnOffsets) {
        this.entries = entries;
        this.columnOffsets = columnOffsets;
    }

    @Override
    public long getAddress() {
        return currentValueOffset;
    }

    @Override
    public boolean getBool(int columnIndex) {
        return entries.getBool(getValueColumnOffset(columnIndex));
    }

    @Override
    public byte getByte(int columnIndex) {
        return entries.getByte(getValueColumnOffset(columnIndex));
    }

    @Override
    public long getDate(int columnIndex) {
        return getLong(columnIndex);
    }

    @Override
    public double getDouble(int columnIndex) {
        return entries.getDouble(getValueColumnOffset(columnIndex));
    }

    @Override
    public float getFloat(int columnIndex) {
        return entries.getFloat(getValueColumnOffset(columnIndex));
    }

    @Override
    public int getInt(int columnIndex) {
        return entries.getInt(getValueColumnOffset(columnIndex));
    }

    @Override
    public long getLong(int columnIndex) {
        return entries.getLong(getValueColumnOffset(columnIndex));
    }

    @Override
    public void addLong(int index, long value) {
        final long o = getValueColumnOffset(index);
        entries.putLong(o, entries.getLong(o) + value);
    }

    @Override
    public void addByte(int index, byte value) {
        final long o = getValueColumnOffset(index);
        entries.putByte(o, (byte) (entries.getByte(o) + value));
    }

    @Override
    public void addShort(int index, short value) {
        final long o = getValueColumnOffset(index);
        entries.putShort(o, (short) (entries.getShort(o) + value));
    }

    @Override
    public void addInt(int index, int value) {
        final long o = getValueColumnOffset(index);
        entries.putInt(o, entries.getInt(o) + value);
    }

    @Override
    public void addDouble(int index, double value) {
        final long o = getValueColumnOffset(index);
        entries.putDouble(o, entries.getDouble(o) + value);
    }

    @Override
    public void addFloat(int index, float value) {
        final long o = getValueColumnOffset(index);
        entries.putFloat(o, entries.getFloat(o) + value);
    }

    @Override
    public short getShort(int columnIndex) {
        return entries.getShort(getValueColumnOffset(columnIndex));
    }

    @Override
    public char getChar(int columnIndex) {
        return entries.getChar(getValueColumnOffset(columnIndex));
    }

    @Override
    public long getTimestamp(int columnIndex) {
        return getLong(columnIndex);
    }

    @Override
    public boolean isNew() {
        return _new;
    }

    @Override
    public void putBool(int columnIndex, boolean value) {
        entries.putBool(getValueColumnOffset(columnIndex), value);
    }

    @Override
    public void putByte(int columnIndex, byte value) {
        entries.putByte(getValueColumnOffset(columnIndex), value);
    }

    @Override
    public void putDate(int columnIndex, long value) {
        putLong(columnIndex, value);
    }

    @Override
    public void putDouble(int columnIndex, double value) {
        entries.putDouble(getValueColumnOffset(columnIndex), value);
    }

    @Override
    public void putFloat(int columnIndex, float value) {
        entries.putFloat(getValueColumnOffset(columnIndex), value);
    }

    @Override
    public void putInt(int columnIndex, int value) {
        entries.putInt(getValueColumnOffset(columnIndex), value);
    }

    @Override
    public void putLong(int columnIndex, long value) {
        entries.putLong(getValueColumnOffset(columnIndex), value);
    }

    @Override
    public void putShort(int columnIndex, short value) {
        entries.putShort(getValueColumnOffset(columnIndex), value);
    }

    @Override
    public void putChar(int columnIndex, char value) {
        entries.putChar(getValueColumnOffset(columnIndex), value);
    }

    @Override
    public void putTimestamp(int columnIndex, long value) {
        putLong(columnIndex, value);
    }

    @Override
    public void setMapRecordHere() {
        record.of(currentValueOffset);
    }

    private long getValueColumnOffset(int columnIndex) {
        assert currentValueOffset != -1;
        return currentValueOffset + columnOffsets[columnIndex];
    }

    void linkRecord(CompactMapRecord record) {
        this.record = record;
    }

    void of(long offset, boolean _new) {
        this.currentValueOffset = offset;
        this._new = _new;
    }
}
