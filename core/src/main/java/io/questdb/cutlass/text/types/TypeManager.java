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

package io.questdb.cutlass.text.types;

import io.questdb.cairo.CairoException;
import io.questdb.cairo.ColumnType;
import io.questdb.cutlass.text.TextConfiguration;
import io.questdb.std.IntList;
import io.questdb.std.Mutable;
import io.questdb.std.ObjList;
import io.questdb.std.ObjectPool;
import io.questdb.std.microtime.TimestampFormat;
import io.questdb.std.microtime.TimestampLocale;
import io.questdb.std.str.DirectCharSink;
import io.questdb.std.time.DateFormat;
import io.questdb.std.time.DateLocale;

public class TypeManager implements Mutable {
    private final ObjList<TypeAdapter> probes = new ObjList<>();
    private final int probeCount;
    private final StringAdapter stringAdapter;
    private final ObjectPool<DateUtf8Adapter> dateAdapterPool;
    private final ObjectPool<TimestampUtf8Adapter> timestampUtf8AdapterPool;
    private final ObjectPool<TimestampAdapter> timestampAdapterPool;
    private final SymbolAdapter symbolAdapter;
    private final InputFormatConfiguration inputFormatConfiguration;

    public TypeManager(
            TextConfiguration configuration,
            DirectCharSink utf8Sink
    ) {
        this.dateAdapterPool = new ObjectPool<>(() -> new DateUtf8Adapter(utf8Sink), configuration.getDateAdapterPoolCapacity());
        this.timestampUtf8AdapterPool = new ObjectPool<>(() -> new TimestampUtf8Adapter(utf8Sink), configuration.getTimestampAdapterPoolCapacity());
        this.timestampAdapterPool = new ObjectPool<>(TimestampAdapter::new, configuration.getTimestampAdapterPoolCapacity());
        this.inputFormatConfiguration = configuration.getInputFormatConfiguration();
        this.stringAdapter = new StringAdapter(utf8Sink);
        this.symbolAdapter = new SymbolAdapter(utf8Sink);
        addDefaultProbes();

        final ObjList<DateFormat> dateFormats = inputFormatConfiguration.getDateFormats();
        final ObjList<DateLocale> dateLocales = inputFormatConfiguration.getDateLocales();
        final IntList dateUtf8Flags = inputFormatConfiguration.getDateUtf8Flags();
        for (int i = 0, n = dateFormats.size(); i < n; i++) {
            if (dateUtf8Flags.getQuick(i) == 1) {
                probes.add(new DateUtf8Adapter(utf8Sink).of(dateFormats.getQuick(i), dateLocales.getQuick(i)));
            } else {
                probes.add(new DateAdapter().of(dateFormats.getQuick(i), dateLocales.getQuick(i)));
            }
        }
        final ObjList<TimestampFormat> timestampFormats = inputFormatConfiguration.getTimestampFormats();
        final ObjList<TimestampLocale> timestampLocales = inputFormatConfiguration.getTimestampLocales();
        final IntList timestampUtf8Flags = inputFormatConfiguration.getTimestampUtf8Flags();
        for (int i = 0, n = timestampFormats.size(); i < n; i++) {
            if (timestampUtf8Flags.getQuick(i) == 1) {
                probes.add(new TimestampUtf8Adapter(utf8Sink).of(timestampFormats.getQuick(i), timestampLocales.getQuick(i)));
            } else {
                probes.add(new TimestampAdapter().of(timestampFormats.getQuick(i), timestampLocales.getQuick(i)));
            }
        }
        this.probeCount = probes.size();
    }

    @Override
    public void clear() {
        dateAdapterPool.clear();
        timestampUtf8AdapterPool.clear();
        timestampAdapterPool.clear();
    }

    public InputFormatConfiguration getInputFormatConfiguration() {
        return inputFormatConfiguration;
    }

    public TypeAdapter getProbe(int index) {
        return probes.getQuick(index);
    }

    public int getProbeCount() {
        return probeCount;
    }

    public TypeAdapter getTypeAdapter(int columnType) {
        switch (columnType) {
            case ColumnType.BYTE:
                return ByteAdapter.INSTANCE;
            case ColumnType.SHORT:
                return ShortAdapter.INSTANCE;
            case ColumnType.CHAR:
                return CharAdapter.INSTANCE;
            case ColumnType.INT:
                return IntAdapter.INSTANCE;
            case ColumnType.LONG:
                return LongAdapter.INSTANCE;
            case ColumnType.BOOLEAN:
                return BooleanAdapter.INSTANCE;
            case ColumnType.FLOAT:
                return FloatAdapter.INSTANCE;
            case ColumnType.DOUBLE:
                return DoubleAdapter.INSTANCE;
            case ColumnType.STRING:
                return stringAdapter;
            case ColumnType.SYMBOL:
                return symbolAdapter;
            case ColumnType.LONG256:
                return Long256Adapter.INSTANCE;
            default:
                throw CairoException.instance(0).put("no adapter for type [id=").put(columnType).put(", name=").put(ColumnType.nameOf(columnType)).put(']');
        }
    }

    public DateUtf8Adapter nextDateAdapter() {
        return dateAdapterPool.next();
    }

    public TypeAdapter nextTimestampAdapter(boolean decodeUtf8, TimestampFormat format, TimestampLocale locale) {
        if (decodeUtf8) {
            TimestampUtf8Adapter adapter = timestampUtf8AdapterPool.next();
            adapter.of(format, locale);
            return adapter;
        }

        TimestampAdapter adapter = timestampAdapterPool.next();
        adapter.of(format, locale);
        return adapter;
    }

    private void addDefaultProbes() {
        probes.add(getTypeAdapter(ColumnType.CHAR));
        probes.add(getTypeAdapter(ColumnType.INT));
        probes.add(getTypeAdapter(ColumnType.LONG));
        probes.add(getTypeAdapter(ColumnType.DOUBLE));
        probes.add(getTypeAdapter(ColumnType.BOOLEAN));
        probes.add(getTypeAdapter(ColumnType.LONG256));
    }

    ObjList<TypeAdapter> getAllAdapters() {
        return probes;
    }
}