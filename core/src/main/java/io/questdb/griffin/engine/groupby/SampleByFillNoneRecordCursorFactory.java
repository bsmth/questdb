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

import io.questdb.cairo.ArrayColumnTypes;
import io.questdb.cairo.CairoConfiguration;
import io.questdb.cairo.ListColumnFilter;
import io.questdb.cairo.RecordSink;
import io.questdb.cairo.map.Map;
import io.questdb.cairo.sql.DelegatingRecordCursor;
import io.questdb.cairo.sql.Function;
import io.questdb.cairo.sql.RecordCursor;
import io.questdb.cairo.sql.RecordCursorFactory;
import io.questdb.griffin.FunctionParser;
import io.questdb.griffin.SqlException;
import io.questdb.griffin.SqlExecutionContext;
import io.questdb.griffin.engine.EmptyTableRecordCursor;
import io.questdb.griffin.engine.functions.GroupByFunction;
import io.questdb.griffin.model.QueryModel;
import io.questdb.std.BytecodeAssembler;
import io.questdb.std.IntIntHashMap;
import io.questdb.std.ObjList;
import io.questdb.std.Transient;
import org.jetbrains.annotations.NotNull;

public class SampleByFillNoneRecordCursorFactory extends AbstractSampleByRecordCursorFactory {
    private final static SampleByCursorLambda CURSOR_LAMBDA = SampleByFillNoneRecordCursorFactory::createCursor;

    public SampleByFillNoneRecordCursorFactory(
            CairoConfiguration configuration,
            RecordCursorFactory base,
            @NotNull TimestampSampler timestampSampler,
            @Transient @NotNull QueryModel model,
            @Transient @NotNull ListColumnFilter listColumnFilter,
            @Transient @NotNull FunctionParser functionParser,
            @Transient @NotNull SqlExecutionContext executionContext,
            @Transient @NotNull BytecodeAssembler asm,
            @Transient @NotNull ArrayColumnTypes keyTypes,
            @Transient @NotNull ArrayColumnTypes valueTypes
    ) throws SqlException {
        super(
                configuration,
                base,
                timestampSampler,
                model,
                listColumnFilter,
                functionParser,
                executionContext,
                asm,
                CURSOR_LAMBDA,
                keyTypes,
                valueTypes
        );
    }

    @Override
    public RecordCursor getCursor(SqlExecutionContext executionContext) {
        final RecordCursor baseCursor = base.getCursor(executionContext);
        if (baseCursor.hasNext()) {
            map.clear();
            return initFunctionsAndCursor(executionContext, baseCursor);
        }

        baseCursor.close();
        return EmptyTableRecordCursor.INSTANCE;
    }

    @NotNull
    private static DelegatingRecordCursor createCursor(
            Map map,
            RecordSink mapSink,
            @NotNull TimestampSampler timestampSampler,
            int timestampIndex,
            ObjList<GroupByFunction> groupByFunctions,
            ObjList<Function> recordFunctions,
            IntIntHashMap symbolTableIndex,
            int keyCount
    ) {
        if (keyCount == 0) {
            return new SampleByFillNoneNKRecordCursor(
                    map,
                    groupByFunctions,
                    recordFunctions,
                    timestampIndex,
                    timestampSampler,
                    symbolTableIndex
            );
        }
        return new SampleByFillNoneRecordCursor(
                map,
                mapSink,
                groupByFunctions,
                recordFunctions,
                timestampIndex,
                timestampSampler,
                symbolTableIndex
        );
    }
}
