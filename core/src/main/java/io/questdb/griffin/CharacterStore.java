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

package io.questdb.griffin;

import io.questdb.log.Log;
import io.questdb.log.LogFactory;
import io.questdb.std.Mutable;
import io.questdb.std.Numbers;
import io.questdb.std.ObjectPool;
import io.questdb.std.Unsafe;
import io.questdb.std.str.AbstractCharSequence;
import io.questdb.std.str.AbstractCharSink;
import io.questdb.std.str.CharSink;

public class CharacterStore extends AbstractCharSink implements CharacterStoreEntry, Mutable {
    private static final Log LOG = LogFactory.getLog(CharacterStore.class);
    private final ObjectPool<NameAssemblerCharSequence> csPool;
    private int capacity;
    private char[] chars;
    private int size = 0;
    private NameAssemblerCharSequence next = null;

    public CharacterStore(int capacity, int poolCapacity) {
        this.capacity = capacity;
        this.chars = new char[Numbers.ceilPow2(capacity)];
        csPool = new ObjectPool<>(NameAssemblerCharSequence::new, poolCapacity);
    }

    @Override
    public int length() {
        return size;
    }

    @Override
    public CharSequence toImmutable() {
        next.hi = size;
        return next;
    }

    public void trimTo(int size) {
        this.size = size;
    }

    public CharacterStoreEntry newEntry() {
        this.next = csPool.next();
        this.next.lo = size;
        return this;
    }

    @Override
    public CharSink put(CharSequence cs) {
        assert cs != null;
        return put(cs, 0, cs.length());
    }

    @Override
    public CharSink put(CharSequence cs, int start, int end) {
        for (int i = start; i < end; i++) {
            put(cs.charAt(i));
        }
        return this;
    }

    @Override
    public CharSink put(char c) {
        if (size < capacity) {
            Unsafe.arrayPut(chars, size++, c);
        } else {
            resizeAndPut(c);
        }
        return this;
    }

    private void resizeAndPut(char c) {
        char[] next = new char[capacity * 2];
        System.arraycopy(chars, 0, next, 0, capacity);
        chars = next;
        capacity *= 2;
        Unsafe.arrayPut(chars, size++, c);
        LOG.info().$("resize [capacity=").$(capacity).$(']').$();
    }

    @Override
    public void clear() {
        csPool.clear();
        size = 0;
        next = null;
    }

    private class NameAssemblerCharSequence extends AbstractCharSequence implements Mutable {
        int lo;
        int hi;

        @Override
        public void clear() {
        }

        @Override
        public int length() {
            return hi - lo;
        }

        @Override
        public char charAt(int index) {
            return chars[lo + index];
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            NameAssemblerCharSequence that = csPool.next();
            that.lo = lo + start;
            that.hi = lo + end;
            assert that.lo < that.hi;
            return that;
        }
    }
}
