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

package io.questdb.griffin.engine.groupby;

import io.questdb.cairo.map.Map;
import io.questdb.cairo.map.MapValue;
import io.questdb.cairo.sql.*;
import io.questdb.griffin.engine.functions.GroupByFunction;
import io.questdb.griffin.engine.functions.TimestampFunction;
import io.questdb.std.IntIntHashMap;
import io.questdb.std.ObjList;

class SampleByFillNoneNKRecordCursor implements DelegatingRecordCursor, NoRandomAccessRecordCursor {
    private final Map map;
    private final ObjList<GroupByFunction> groupByFunctions;
    private final int timestampIndex;
    private final TimestampSampler timestampSampler;
    private final Record record;
    private final IntIntHashMap symbolTableIndex;
    private final RecordCursor mapCursor;
    private RecordCursor base;
    private Record baseRecord;
    private long lastTimestamp;
    private long nextTimestamp;

    public SampleByFillNoneNKRecordCursor(
            Map map,
            ObjList<GroupByFunction> groupByFunctions,
            ObjList<Function> recordFunctions,
            int timestampIndex, // index of timestamp column in base cursor
            TimestampSampler timestampSampler,
            IntIntHashMap symbolTableIndex) {
        this.map = map;
        this.groupByFunctions = groupByFunctions;
        this.timestampIndex = timestampIndex;
        this.timestampSampler = timestampSampler;
        VirtualRecord rec = new VirtualRecordNoRowid(recordFunctions);
        rec.of(map.getRecord());
        this.record = rec;
        this.symbolTableIndex = symbolTableIndex;
        for (int i = 0, n = recordFunctions.size(); i < n; i++) {
            Function f = recordFunctions.getQuick(i);
            if (f == null) {
                recordFunctions.setQuick(i, new TimestampFunc(0));
            }
        }
        this.mapCursor = map.getCursor();
    }

    @Override
    public void close() {
        base.close();
    }

    @Override
    public Record getRecord() {
        return record;
    }

    @Override
    public SymbolTable getSymbolTable(int columnIndex) {
        return base.getSymbolTable(symbolTableIndex.get(columnIndex));
    }

    @Override
    public boolean hasNext() {
        return mapHasNext() || baseRecord != null && computeNextBatch();
    }

    @Override
    public void toTop() {
        this.base.toTop();
        if (base.hasNext()) {
            baseRecord = base.getRecord();
            this.nextTimestamp = timestampSampler.round(baseRecord.getTimestamp(timestampIndex));
            this.lastTimestamp = this.nextTimestamp;
            map.clear();
        }
    }

    @Override
    public long size() {
        return -1;
    }

    public void of(RecordCursor base) {
        // factory guarantees that base cursor is not empty
        this.base = base;
        this.baseRecord = base.getRecord();
        this.nextTimestamp = timestampSampler.round(baseRecord.getTimestamp(timestampIndex));
        this.lastTimestamp = this.nextTimestamp;
    }

    private boolean computeNextBatch() {
        this.lastTimestamp = this.nextTimestamp;
        this.map.clear();
        final MapValue value = map.withKey().createValue();

        // looks like we need to populate key map
        // at the start of this loop 'lastTimestamp' will be set to timestamp
        // of first record in base cursor
        int n = groupByFunctions.size();
        do {
            final long timestamp = timestampSampler.round(baseRecord.getTimestamp(timestampIndex));
            if (lastTimestamp == timestamp) {
                GroupByUtils.updateFunctions(groupByFunctions, n, value, baseRecord);
                if (value.isNew()) {
                    map.withKey().createValue();
                }
            } else {
                // timestamp changed, make sure we keep the value of 'lastTimestamp'
                // unchanged. Timestamp columns uses this variable
                // When map is exhausted we would assign 'nextTimestamp' to 'lastTimestamp'
                // and build another map
                this.nextTimestamp = timestamp;
                return createMapCursor();
            }
        } while (base.hasNext());

        // we ran out of data, make sure hasNext() returns false at the next
        // opportunity, after we stream map that is.
        baseRecord = null;
        return createMapCursor();
    }

    private boolean createMapCursor() {
        // reset map iterator
        map.getCursor();
        // we do not have any more data, let map take over
        return mapHasNext();
    }

    private boolean mapHasNext() {
        return mapCursor.hasNext();
    }

    private class TimestampFunc extends TimestampFunction {

        public TimestampFunc(int position) {
            super(position);
        }

        @Override
        public long getTimestamp(Record rec) {
            return lastTimestamp;
        }
    }
}
