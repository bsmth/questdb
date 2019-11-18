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

package io.questdb.std;

public class LowerCaseAsciiCharSequenceHashSet extends AbstractLowerCaseAsciiCharSequenceHashSet {

    private static final int MIN_INITIAL_CAPACITY = 16;

    public LowerCaseAsciiCharSequenceHashSet() {
        this(MIN_INITIAL_CAPACITY);
    }

    private LowerCaseAsciiCharSequenceHashSet(int initialCapacity) {
        this(initialCapacity, 0.4);
    }

    private LowerCaseAsciiCharSequenceHashSet(int initialCapacity, double loadFactor) {
        super(initialCapacity, loadFactor);
        clear();
    }

    /**
     * Adds key to hash set preserving key uniqueness.
     *
     * @param key immutable sequence of characters.
     * @return false if key is already in the set and true otherwise.
     */
    public boolean add(CharSequence key) {
        int index = keyIndex(key);
        if (index < 0) {
            return false;
        }

        addAt(index, key);
        return true;
    }

    public void addAt(int index, CharSequence key) {
        String s = key.toString().toLowerCase();
        Unsafe.arrayPut(keys, index, s);
        if (--free < 1) {
            rehash();
        }
    }

    public boolean contains(CharSequence key) {
        return keyIndex(key) < 0;
    }

    public CharSequence keyAt(int index) {
        return keys[-index - 1];
    }

    @Override
    protected void erase(int index) {
        Unsafe.arrayPut(keys, index, noEntryKey);
    }

    @Override
    protected void move(int from, int to) {
        Unsafe.arrayPut(keys, to, keys[from]);
        erase(from);
    }

    private void rehash() {
        int newCapacity = capacity * 2;
        final int size = size();
        mask = newCapacity - 1;
        free = capacity = newCapacity;
        int arrayCapacity = (int) (newCapacity / loadFactor);

        CharSequence[] newKeys = new CharSequence[arrayCapacity];
        CharSequence[] oldKeys = keys;
        this.keys = newKeys;
        free -= size;
        for (int i = 0, n = oldKeys.length; i < n; i++) {
            CharSequence key = oldKeys[i];
            if (key != null) {
                Unsafe.arrayPut(keys, keyIndex(key), key);
            }
        }
    }
}