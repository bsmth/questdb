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

import io.questdb.cairo.ColumnType;
import io.questdb.cairo.TableWriter;
import io.questdb.std.Mutable;
import io.questdb.std.NumericException;
import io.questdb.std.str.DirectByteCharSequence;
import io.questdb.std.time.DateFormat;
import io.questdb.std.time.DateLocale;

public class DateAdapter extends AbstractTypeAdapter implements Mutable {
    private DateLocale locale;
    private DateFormat format;

    @Override
    public void clear() {
        this.format = null;
        this.locale = null;
    }

    @Override
    public int getType() {
        return ColumnType.DATE;
    }

    @Override
    public boolean probe(CharSequence text) {
        try {
            format.parse(text, locale);
            return true;
        } catch (NumericException e) {
            return false;
        }
    }

    @Override
    public void write(TableWriter.Row row, int column, DirectByteCharSequence value) throws Exception {
        row.putDate(column, format.parse(value, locale));
    }

    public DateAdapter of(DateFormat format, DateLocale locale) {
        this.format = format;
        this.locale = locale;
        return this;
    }
}
