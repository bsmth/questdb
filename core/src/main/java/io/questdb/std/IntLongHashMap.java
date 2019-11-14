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

package io.questdb.std;

import java.util.Arrays;

public class IntLongHashMap extends AbstractIntHashSet {
    private static final int noEntryValue = -1;
    private long[] values;

    public IntLongHashMap() {
        this(8);
    }

    public IntLongHashMap(int initialCapacity) {
        this(initialCapacity, 0.5f);
    }

    private IntLongHashMap(int initialCapacity, double loadFactor) {
        super(initialCapacity, loadFactor);
        values = new long[keys.length];
        clear();
    }

    public long get(int key) {
        return valueAt(keyIndex(key));
    }

    public void put(int key, long value) {
        putAt(keyIndex(key), key, value);
    }

    public void putAt(int index, int key, long value) {
        if (index < 0) {
            Unsafe.arrayPut(values, -index - 1, value);
        } else {
            Unsafe.arrayPut(keys, index, key);
            Unsafe.arrayPut(values, index, value);
            if (--free == 0) {
                rehash();
            }
        }
    }

    public long valueAt(int index) {
        return index < 0 ? values[-index - 1] : noEntryValue;
    }

    @Override
    protected void erase(int index) {
        Unsafe.arrayPut(keys, index, this.noEntryKeyValue);
    }

    @Override
    protected void move(int from, int to) {
        Unsafe.arrayPut(keys, to, keys[from]);
        Unsafe.arrayPut(values, to, values[from]);
        erase(from);
    }

    private void rehash() {
        int size = size();
        int newCapacity = capacity * 2;
        mask = newCapacity - 1;
        free = capacity = newCapacity;
        int arrayCapacity = (int) (newCapacity / loadFactor);

        long[] oldValues = values;
        int[] oldKeys = keys;
        this.keys = new int[arrayCapacity];
        this.values = new long[arrayCapacity];
        Arrays.fill(keys, noEntryKeyValue);

        free -= size;
        for (int i = oldKeys.length; i-- > 0; ) {
            int key = oldKeys[i];
            if (key != noEntryKeyValue) {
                final int index = keyIndex(key);
                Unsafe.arrayPut(keys, index, key);
                Unsafe.arrayPut(values, index, oldValues[i]);
            }
        }
    }
}
