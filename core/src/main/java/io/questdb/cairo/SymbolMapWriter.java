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

package io.questdb.cairo;

import io.questdb.cairo.sql.RowCursor;
import io.questdb.cairo.sql.SymbolTable;
import io.questdb.log.Log;
import io.questdb.log.LogFactory;
import io.questdb.std.*;
import io.questdb.std.str.Path;

import java.io.Closeable;

public class SymbolMapWriter implements Closeable {
    public static final int HEADER_SIZE = 64;
    private static final Log LOG = LogFactory.getLog(SymbolMapWriter.class);

    private final BitmapIndexWriter indexWriter;
    private final ReadWriteMemory charMem;
    private final ReadWriteMemory offsetMem;
    private final CharSequenceIntHashMap cache;
    private final int maxHash;

    public SymbolMapWriter(CairoConfiguration configuration, Path path, CharSequence name, int symbolCount) {
        final int plen = path.length();
        try {
            final FilesFacade ff = configuration.getFilesFacade();
            final long mapPageSize = ff.getMapPageSize();

            // this constructor does not create index. Index must exist
            // and we use "offset" file to store "header"
            offsetFileName(path.trimTo(plen), name);
            if (!ff.exists(path)) {
                LOG.error().$(path).$(" is not found").$();
                throw CairoException.instance(0).put("SymbolMap does not exist: ").put(path);
            }

            // is there enough length in "offset" file for "header"?
            long len = ff.length(path);
            if (len < HEADER_SIZE) {
                LOG.error().$(path).$(" is too short [len=").$(len).$(']').$();
                throw CairoException.instance(0).put("SymbolMap is too short: ").put(path);
            }

            // open "offset" memory and make sure we start appending from where
            // we left off. Where we left off is stored externally to symbol map
            this.offsetMem = new ReadWriteMemory(ff, path, mapPageSize);
            final int symbolCapacity = offsetMem.getInt(0);
            final boolean useCache = offsetMem.getBool(4);
            this.offsetMem.jumpTo(keyToOffset(symbolCount));

            // index writer is used to identify attempts to store duplicate symbol value
            this.indexWriter = new BitmapIndexWriter(configuration, path.trimTo(plen), name);

            // this is the place where symbol values are stored
            this.charMem = new ReadWriteMemory(ff, charFileName(path.trimTo(plen), name), mapPageSize);

            // move append pointer for symbol values in the correct place
            jumpCharMemToSymbolCount(symbolCount);

            // we use index hash maximum equals to half of symbol capacity, which
            // theoretically should require 2 value cells in index per hash
            // we use 4 cells to compensate for occasionally unlucky hash distribution
            this.maxHash = Numbers.ceilPow2(symbolCapacity / 2) - 1;

            if (useCache) {
                this.cache = new CharSequenceIntHashMap(symbolCapacity);
            } else {
                this.cache = null;
            }
            LOG.info().$("open [name=").$(path.trimTo(plen).concat(name).$()).$(", fd=").$(this.offsetMem.getFd()).$(", cache=").$(cache != null).$(", capacity=").$(symbolCapacity).$(']').$();
        } catch (CairoException e) {
            close();
            throw e;
        } finally {
            path.trimTo(plen);
        }
    }

    public static Path charFileName(Path path, CharSequence columnName) {
        return path.concat(columnName).put(".c").$();
    }

    public static void createSymbolMapFiles(FilesFacade ff, AppendMemory mem, Path path, CharSequence columnName, int symbolCapacity, boolean symbolCacheFlag) {
        int plen = path.length();
        try {
            mem.of(ff, offsetFileName(path.trimTo(plen), columnName), ff.getPageSize());
            mem.putInt(symbolCapacity);
            mem.putBool(symbolCacheFlag);
            mem.jumpTo(HEADER_SIZE);
            mem.close();

            if (!ff.touch(charFileName(path.trimTo(plen), columnName))) {
                throw CairoException.instance(ff.errno()).put("Cannot create ").put(path);
            }

            mem.of(ff, BitmapIndexUtils.keyFileName(path.trimTo(plen), columnName), ff.getPageSize());
            BitmapIndexWriter.initKeyMemory(mem, TableUtils.MIN_INDEX_VALUE_BLOCK_SIZE);
            ff.touch(BitmapIndexUtils.valueFileName(path.trimTo(plen), columnName));
        } finally {
            path.trimTo(plen);
            mem.close();
        }
    }

    public static Path offsetFileName(Path path, CharSequence columnName) {
        return path.concat(columnName).put(".o").$();
    }

    static int offsetToKey(long offset) {
        return (int) ((offset - HEADER_SIZE) / 8L);
    }

    static long keyToOffset(int key) {
        return HEADER_SIZE + key * 8L;
    }

    @Override
    public void close() {
        Misc.free(indexWriter);
        Misc.free(charMem);
        if (this.offsetMem != null) {
            long fd = this.offsetMem.getFd();
            Misc.free(offsetMem);
            LOG.info().$("closed [fd=").$(fd).$(']').$();
        }
    }

    public int getSymbolCount() {
        return offsetToKey(offsetMem.getAppendOffset());
    }

    public int put(CharSequence symbol) {

        if (symbol == null) {
            return SymbolTable.VALUE_IS_NULL;
        }

        if (cache != null) {
            int index = cache.keyIndex(symbol);
            return index < 0 ? cache.valueAt(index) : lookupPutAndCache(index, symbol);
        }
        return lookupAndPut(symbol);
    }

    public void rollback(int symbolCount) {
        indexWriter.rollbackValues(keyToOffset(symbolCount));
        offsetMem.jumpTo(keyToOffset(symbolCount));
        jumpCharMemToSymbolCount(symbolCount);
        if (cache != null) {
            cache.clear();
        }
    }

    boolean isCached() {
        return cache != null;
    }

    private void jumpCharMemToSymbolCount(int symbolCount) {
        if (symbolCount > 0) {
            long lastSymbolOffset = this.offsetMem.getLong(keyToOffset(symbolCount - 1));
            int l = VirtualMemory.getStorageLength(this.charMem.getStr(lastSymbolOffset));
            this.charMem.jumpTo(lastSymbolOffset + l);
        } else {
            this.charMem.jumpTo(0);
        }
    }

    private int lookupAndPut(CharSequence symbol) {
        int hash = Hash.boundedHash(symbol, maxHash);
        RowCursor cursor = indexWriter.getCursor(hash);
        while (cursor.hasNext()) {
            long offsetOffset = cursor.next();
            if (Chars.equals(symbol, charMem.getStr(offsetMem.getLong(offsetOffset)))) {
                return offsetToKey(offsetOffset);
            }
        }
        return put0(symbol, hash);
    }

    private int lookupPutAndCache(int index, CharSequence symbol) {
        int result;
        result = lookupAndPut(symbol);
        cache.putAt(index, symbol.toString(), result);
        return result;
    }

    private int put0(CharSequence symbol, int hash) {
        long offsetOffset = offsetMem.getAppendOffset();
        offsetMem.putLong(charMem.putStr(symbol));
        indexWriter.add(hash, offsetOffset);
        return offsetToKey(offsetOffset);
    }
}
