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

public class Long256Impl implements Long256Sink, Long256 {

    public static final Long256Impl NULL_LONG256 = new Long256Impl();

    static {
        NULL_LONG256.setLong0(-1);
        NULL_LONG256.setLong1(-1);
        NULL_LONG256.setLong2(-1);
        NULL_LONG256.setLong3(-1);
    }

    private long l0;
    private long l1;
    private long l2;
    private long l3;

    @Override
    public long getLong0() {
        return l0;
    }

    @Override
    public void setLong0(long value) {
        this.l0 = value;
    }

    @Override
    public long getLong1() {
        return l1;
    }

    @Override
    public void setLong1(long value) {
        this.l1 = value;
    }

    @Override
    public long getLong2() {
        return l2;
    }

    @Override
    public void setLong2(long value) {
        this.l2 = value;
    }

    @Override
    public long getLong3() {
        return l3;
    }

    @Override
    public void setLong3(long value) {
        this.l3 = value;
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object obj) {
        final Long256Impl that = (Long256Impl) obj;
        return l0 == that.l0 && l1 == that.l1 && l2 == that.l2 && l3 == that.l3;
    }

    public void copyFrom(Long256 value) {
        this.l0 = value.getLong0();
        this.l1 = value.getLong1();
        this.l2 = value.getLong2();
        this.l3 = value.getLong3();
    }

    public void setAll(long l0, long l1, long l2, long l3) {
        this.l0 = l0;
        this.l1 = l1;
        this.l2 = l2;
        this.l3 = l3;
    }
}
