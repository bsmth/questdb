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

package io.questdb.griffin;

import io.questdb.griffin.engine.functions.rnd.SharedRandom;
import io.questdb.std.Rnd;
import io.questdb.std.datetime.microtime.Timestamps;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.function.LongPredicate;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class TimestampQueryTest extends AbstractGriffinTest {

    @Before
    public void setUp3() {
        SharedRandom.RANDOM.set(new Rnd());
    }

    @Test
    public void testEqualityTimestampFormatYearAndMonthNegativeTest() throws Exception {
        assertMemoryLeak(() -> {
            //create table
            String createStmt = "create table ob_mem_snapshot (symbol int,  me_seq_num long,  timestamp timestamp) timestamp(timestamp) partition by DAY";
            compiler.compile(createStmt, sqlExecutionContext);
            //insert
            executeInsert("INSERT INTO ob_mem_snapshot  VALUES(1, 1, 1609459199000000)");
            String expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            String query = "select * from ob_mem_snapshot";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
            // test where ts ='2021-01'
            expected = "symbol\tme_seq_num\ttimestamp\n";
            query = "SELECT * FROM ob_mem_snapshot where timestamp ='2021-01'";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
            // test where ts ='2020-11'
            expected = "symbol\tme_seq_num\ttimestamp\n";
            query = "SELECT * FROM ob_mem_snapshot where timestamp ='2020-11'";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
        });
    }

    @Test
    public void testEqualityTimestampFormatYearAndMonthPositiveTest() throws Exception {
        assertMemoryLeak(() -> {
            //create table
            String createStmt = "create table ob_mem_snapshot (symbol int,  me_seq_num long,  timestamp timestamp) timestamp(timestamp) partition by DAY";
            compiler.compile(createStmt, sqlExecutionContext);
            //insert
            executeInsert("INSERT INTO ob_mem_snapshot  VALUES(1, 1, 1609459199000000)");
            String expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            String query = "select * from ob_mem_snapshot";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
            // test where ts ='2020-12'
            expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            query = "SELECT * FROM ob_mem_snapshot where timestamp ='2020-12'";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
        });
    }

    @Test
    public void testEqualityTimestampFormatYearOnlyNegativeTest() throws Exception {
        assertMemoryLeak(() -> {
            //create table
            String createStmt = "create table ob_mem_snapshot (symbol int,  me_seq_num long,  timestamp timestamp) timestamp(timestamp) partition by DAY";
            compiler.compile(createStmt, sqlExecutionContext);
            //insert
            executeInsert("INSERT INTO ob_mem_snapshot  VALUES(1, 1, 1609459199000000)");
            String expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            String query = "select * from ob_mem_snapshot";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
            // test where ts ='2021'
            expected = "symbol\tme_seq_num\ttimestamp\n";
            query = "SELECT * FROM ob_mem_snapshot where timestamp ='2021'";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
        });
    }

    @Test
    public void testEqualityTimestampFormatYearOnlyPositiveTest() throws Exception {
        assertMemoryLeak(() -> {
            //create table
            String createStmt = "create table ob_mem_snapshot (symbol int,  me_seq_num long,  timestamp timestamp) timestamp(timestamp) partition by DAY";
            compiler.compile(createStmt, sqlExecutionContext);
            //insert
            executeInsert("INSERT INTO ob_mem_snapshot  VALUES(1, 1, 1609459199000000)");
            String expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            String query = "select * from ob_mem_snapshot";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
            // test where ts ='2020'
            expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            query = "SELECT * FROM ob_mem_snapshot where timestamp ='2020'";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
        });
    }

    @Test
    public void testEqualsToTimestampFormatYearMonthDay() throws Exception {
        assertMemoryLeak(() -> {
            //create table
            String createStmt = "create table ob_mem_snapshot (symbol int,  me_seq_num long,  timestamp timestamp) timestamp(timestamp) partition by DAY";
            compiler.compile(createStmt, sqlExecutionContext);
            //insert
            executeInsert("INSERT INTO ob_mem_snapshot  VALUES(1, 1, 1609459199000000)");
            String expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            String query = "select * from ob_mem_snapshot";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
            // test
            expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            query = "SELECT * FROM ob_mem_snapshot where timestamp = '2020-12-31'";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
        });
    }

    @Test
    public void testEqualsToTimestampFormatYearMonthDayHour() throws Exception {
        assertMemoryLeak(() -> {
            //create table
            String createStmt = "create table ob_mem_snapshot (symbol int,  me_seq_num long,  timestamp timestamp) timestamp(timestamp) partition by DAY";
            compiler.compile(createStmt, sqlExecutionContext);
            //insert
            executeInsert("INSERT INTO ob_mem_snapshot  VALUES(1, 1, 1609459199000000)");
            String expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            String query = "select * from ob_mem_snapshot";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
            // test
            expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            query = "SELECT * FROM ob_mem_snapshot where timestamp = '2020-12-31T23'";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
        });
    }

    @Test
    public void testEqualsToTimestampFormatYearMonthDayHourMinute() throws Exception {
        assertMemoryLeak(() -> {
            //create table
            String createStmt = "create table ob_mem_snapshot (symbol int,  me_seq_num long,  timestamp timestamp) timestamp(timestamp) partition by DAY";
            compiler.compile(createStmt, sqlExecutionContext);
            //insert
            executeInsert("INSERT INTO ob_mem_snapshot  VALUES(1, 1, 1609459199000000)");
            String expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            String query = "select * from ob_mem_snapshot";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
            // test
            expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            query = "SELECT * FROM ob_mem_snapshot where timestamp = '2020-12-31T23:59'";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
        });
    }

    @Test
    public void testEqualsToTimestampFormatYearMonthDayHourMinuteSecond() throws Exception {
        assertMemoryLeak(() -> {
            //create table
            String createStmt = "create table ob_mem_snapshot (symbol int,  me_seq_num long,  timestamp timestamp) timestamp(timestamp) partition by DAY";
            compiler.compile(createStmt, sqlExecutionContext);
            //insert
            executeInsert("INSERT INTO ob_mem_snapshot  VALUES(1, 1, 1609459199000000)");
            String expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            String query = "select * from ob_mem_snapshot";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
            // test
            expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            query = "SELECT * FROM ob_mem_snapshot where timestamp = '2020-12-31T23:59:59'";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
        });
    }

    @Test
    public void testEqualsToTimestampWithMicrosecond() throws Exception {
        assertMemoryLeak(() -> {
            //create table
            String createStmt = "create table ob_mem_snapshot (symbol int,  me_seq_num long,  timestamp timestamp) timestamp(timestamp) partition by DAY";
            compiler.compile(createStmt, sqlExecutionContext);
            //insert
            executeInsert("INSERT INTO ob_mem_snapshot  VALUES(1, 1, 1609459199000001)");
            String expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000001Z\n";
            String query = "select * from ob_mem_snapshot";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
            // test
            expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000001Z\n";
            query = "SELECT * FROM ob_mem_snapshot where timestamp = '2020-12-31T23:59:59.000001Z'";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
        });
    }

    @Test
    public void testLMoreThanOrEqualsToTimestampFormatYearOnlyPositiveTest1() throws Exception {
        assertMemoryLeak(() -> {
            //create table
            String createStmt = "create table ob_mem_snapshot (symbol int,  me_seq_num long,  timestamp timestamp) timestamp(timestamp) partition by DAY";
            compiler.compile(createStmt, sqlExecutionContext);
            //insert
            executeInsert("INSERT INTO ob_mem_snapshot  VALUES(1, 1, 1609459199000000)");
            String expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            String query = "select * from ob_mem_snapshot";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
            // test
            expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            query = "SELECT * FROM ob_mem_snapshot where timestamp >= '2020'";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
        });
    }

    @Test
    public void testLMoreThanTimestampFormatYearOnlyPositiveTest1() throws Exception {
        assertMemoryLeak(() -> {
            //create table
            String createStmt = "create table ob_mem_snapshot (symbol int,  me_seq_num long,  timestamp timestamp) timestamp(timestamp) partition by DAY";
            compiler.compile(createStmt, sqlExecutionContext);
            //insert
            executeInsert("INSERT INTO ob_mem_snapshot  VALUES(1, 1, 1609459199000000)");
            String expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            String query = "select * from ob_mem_snapshot";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
            // test
            expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            query = "SELECT * FROM ob_mem_snapshot where timestamp > '2019'";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
        });
    }

    @Test
    public void testLessThanOrEqualsToTimestampFormatYearOnlyNegativeTest1() throws Exception {
        assertMemoryLeak(() -> {
            //create table
            String createStmt = "create table ob_mem_snapshot (symbol int,  me_seq_num long,  timestamp timestamp) timestamp(timestamp) partition by DAY";
            compiler.compile(createStmt, sqlExecutionContext);
            //insert
            executeInsert("INSERT INTO ob_mem_snapshot  VALUES(1, 1, 1609459199000000)");
            String expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            String query = "select * from ob_mem_snapshot";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
            // test
            expected = "symbol\tme_seq_num\ttimestamp\n";
            query = "SELECT * FROM ob_mem_snapshot where timestamp <= '2019'";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
        });
    }

    @Test
    public void testLessThanOrEqualsToTimestampFormatYearOnlyNegativeTest2() throws Exception {
        assertMemoryLeak(() -> {
            //create table
            String createStmt = "create table ob_mem_snapshot (symbol int,  me_seq_num long,  timestamp timestamp) timestamp(timestamp) partition by DAY";
            compiler.compile(createStmt, sqlExecutionContext);
            //insert
            executeInsert("INSERT INTO ob_mem_snapshot  VALUES(1, 1, 1609459199000000)");
            String expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            String query = "select * from ob_mem_snapshot";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
            // test
            expected = "symbol\tme_seq_num\ttimestamp\n";
            query = "SELECT * FROM ob_mem_snapshot where '2021' <=  timestamp";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
        });
    }

    @Test
    public void testLessThanOrEqualsToTimestampFormatYearOnlyPositiveTest1() throws Exception {
        assertMemoryLeak(() -> {
            //create table
            String createStmt = "create table ob_mem_snapshot (symbol int,  me_seq_num long,  timestamp timestamp) timestamp(timestamp) partition by DAY";
            compiler.compile(createStmt, sqlExecutionContext);
            //insert
            executeInsert("INSERT INTO ob_mem_snapshot  VALUES(1, 1, 1609459199000000)");
            String expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            String query = "select * from ob_mem_snapshot";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
            // test
            expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            query = "SELECT * FROM ob_mem_snapshot where timestamp <= '2020'";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
        });
    }

    @Test
    public void testLessThanOrEqualsToTimestampFormatYearOnlyPositiveTest2() throws Exception {
        assertMemoryLeak(() -> {
            //create table
            String createStmt = "create table ob_mem_snapshot (symbol int,  me_seq_num long,  timestamp timestamp) timestamp(timestamp) partition by DAY";
            compiler.compile(createStmt, sqlExecutionContext);
            //insert
            executeInsert("INSERT INTO ob_mem_snapshot  VALUES(1, 1, 1609459199000000)");
            String expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            String query = "select * from ob_mem_snapshot";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
            // test
            expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            query = "SELECT * FROM ob_mem_snapshot where '2020' <=  timestamp";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
        });
    }

    @Test
    public void testLessThanTimestampFormatYearOnlyNegativeTest1() throws Exception {
        assertMemoryLeak(() -> {
            //create table
            String createStmt = "create table ob_mem_snapshot (symbol int,  me_seq_num long,  timestamp timestamp) timestamp(timestamp) partition by DAY";
            compiler.compile(createStmt, sqlExecutionContext);
            //insert
            executeInsert("INSERT INTO ob_mem_snapshot  VALUES(1, 1, 1609459199000000)");
            String expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            String query = "select * from ob_mem_snapshot";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
            // test
            expected = "symbol\tme_seq_num\ttimestamp\n";
            query = "SELECT * FROM ob_mem_snapshot where timestamp <'2020'";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
        });
    }

    @Test
    public void testLessThanTimestampFormatYearOnlyNegativeTest2() throws Exception {
        assertMemoryLeak(() -> {
            //create table
            String createStmt = "create table ob_mem_snapshot (symbol int,  me_seq_num long,  timestamp timestamp) timestamp(timestamp) partition by DAY";
            compiler.compile(createStmt, sqlExecutionContext);
            //insert
            executeInsert("INSERT INTO ob_mem_snapshot  VALUES(1, 1, 1609459199000000)");
            String expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            String query = "select * from ob_mem_snapshot";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
            // test
            expected = "symbol\tme_seq_num\ttimestamp\n";
            query = "SELECT * FROM ob_mem_snapshot where '2021' <  timestamp";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
        });
    }

    @Test
    public void testLessThanTimestampFormatYearOnlyPositiveTest1() throws Exception {
        assertMemoryLeak(() -> {
            //create table
            String createStmt = "create table ob_mem_snapshot (symbol int,  me_seq_num long,  timestamp timestamp) timestamp(timestamp) partition by DAY";
            compiler.compile(createStmt, sqlExecutionContext);
            //insert
            executeInsert("INSERT INTO ob_mem_snapshot  VALUES(1, 1, 1609459199000000)");
            String expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            String query = "select * from ob_mem_snapshot";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
            // test
            expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            query = "SELECT * FROM ob_mem_snapshot where timestamp <'2021'";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
        });
    }

    @Test
    public void testLessThanTimestampFormatYearOnlyPositiveTest2() throws Exception {
        assertMemoryLeak(() -> {
            //create table
            String createStmt = "create table ob_mem_snapshot (symbol int,  me_seq_num long,  timestamp timestamp) timestamp(timestamp) partition by DAY";
            compiler.compile(createStmt, sqlExecutionContext);
            //insert
            executeInsert("INSERT INTO ob_mem_snapshot  VALUES(1, 1, 1609459199000000)");
            String expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            String query = "select * from ob_mem_snapshot";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
            // test
            expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            query = "SELECT * FROM ob_mem_snapshot where '2019' <  timestamp";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
        });
    }

    @Test
    public void testMoreThanOrEqualsToTimestampFormatYearOnlyNegativeTest1() throws Exception {
        assertMemoryLeak(() -> {
            //create table
            String createStmt = "create table ob_mem_snapshot (symbol int,  me_seq_num long,  timestamp timestamp) timestamp(timestamp) partition by DAY";
            compiler.compile(createStmt, sqlExecutionContext);
            //insert
            executeInsert("INSERT INTO ob_mem_snapshot  VALUES(1, 1, 1609459199000000)");
            String expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            String query = "select * from ob_mem_snapshot";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
            // test
            expected = "symbol\tme_seq_num\ttimestamp\n";
            query = "SELECT * FROM ob_mem_snapshot where timestamp >= '2021'";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
        });
    }

    @Test
    public void testMoreThanOrEqualsToTimestampFormatYearOnlyNegativeTest2() throws Exception {
        assertMemoryLeak(() -> {
            //create table
            String createStmt = "create table ob_mem_snapshot (symbol int,  me_seq_num long,  timestamp timestamp) timestamp(timestamp) partition by DAY";
            compiler.compile(createStmt, sqlExecutionContext);
            //insert
            executeInsert("INSERT INTO ob_mem_snapshot  VALUES(1, 1, 1609459199000000)");
            String expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            String query = "select * from ob_mem_snapshot";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
            // test
            expected = "symbol\tme_seq_num\ttimestamp\n";
            query = "SELECT * FROM ob_mem_snapshot where '2019' >=  timestamp";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
        });
    }

    @Test
    public void testMoreThanOrEqualsToTimestampFormatYearOnlyPositiveTest2() throws Exception {
        assertMemoryLeak(() -> {
            //create table
            String createStmt = "create table ob_mem_snapshot (symbol int,  me_seq_num long,  timestamp timestamp) timestamp(timestamp) partition by DAY";
            compiler.compile(createStmt, sqlExecutionContext);
            //insert
            executeInsert("INSERT INTO ob_mem_snapshot  VALUES(1, 1, 1609459199000000)");
            String expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            String query = "select * from ob_mem_snapshot";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
            // test
            expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            query = "SELECT * FROM ob_mem_snapshot where '2020' >=  timestamp";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
        });
    }

    @Test
    public void testMoreThanTimestampFormatYearOnlyNegativeTest1() throws Exception {
        assertMemoryLeak(() -> {
            //create table
            String createStmt = "create table ob_mem_snapshot (symbol int,  me_seq_num long,  timestamp timestamp) timestamp(timestamp) partition by DAY";
            compiler.compile(createStmt, sqlExecutionContext);
            //insert
            executeInsert("INSERT INTO ob_mem_snapshot  VALUES(1, 1, 1609459199000000)");
            String expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            String query = "select * from ob_mem_snapshot";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
            // test
            expected = "symbol\tme_seq_num\ttimestamp\n";
            query = "SELECT * FROM ob_mem_snapshot where timestamp > '2020'";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
        });
    }

    @Test
    public void testMoreThanTimestampFormatYearOnlyNegativeTest2() throws Exception {
        assertMemoryLeak(() -> {
            //create table
            String createStmt = "create table ob_mem_snapshot (symbol int,  me_seq_num long,  timestamp timestamp) timestamp(timestamp) partition by DAY";
            compiler.compile(createStmt, sqlExecutionContext);
            //insert
            executeInsert("INSERT INTO ob_mem_snapshot  VALUES(1, 1, 1609459199000000)");
            String expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            String query = "select * from ob_mem_snapshot";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
            // test
            expected = "symbol\tme_seq_num\ttimestamp\n";
            query = "SELECT * FROM ob_mem_snapshot where '2020' > timestamp";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
        });
    }

    @Test
    public void testMoreThanTimestampFormatYearOnlyPositiveTest2() throws Exception {
        assertMemoryLeak(() -> {
            //create table
            String createStmt = "create table ob_mem_snapshot (symbol int,  me_seq_num long,  timestamp timestamp) timestamp(timestamp) partition by DAY";
            compiler.compile(createStmt, sqlExecutionContext);
            //insert
            executeInsert("INSERT INTO ob_mem_snapshot  VALUES(1, 1, 1609459199000000)");
            String expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            String query = "select * from ob_mem_snapshot";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
            // test
            expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            query = "SELECT * FROM ob_mem_snapshot where '2021' >  timestamp";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
        });
    }

    @Test
    public void testNowIsSameForAllQueryParts() throws Exception {
        try {
            currentMicros = 0;
            assertMemoryLeak(() -> {
                //create table
                String createStmt = "create table ob_mem_snapshot (symbol int,  me_seq_num long,  timestamp timestamp) timestamp(timestamp) partition by DAY";
                compiler.compile(createStmt, sqlExecutionContext);
                //insert
                executeInsert("INSERT INTO ob_mem_snapshot  VALUES(1, 1, 1609459199000000)");
                String expected = "now1\tnow2\tsymbol\ttimestamp\n" +
                        "1970-01-01T00:00:00.000000Z\t1970-01-01T00:00:00.000000Z\t1\t2020-12-31T23:59:59.000000Z\n";

                String query1 = "select now() as now1, now() as now2, symbol, timestamp FROM ob_mem_snapshot WHERE now() = now()";
                printSqlResult(expected, query1, "timestamp", null, null, true, true, false);

                expected = "symbol\tme_seq_num\ttimestamp\n" +
                        "1\t1\t2020-12-31T23:59:59.000000Z\n";
                String query = "select * from ob_mem_snapshot where timestamp > now()";
                printSqlResult(expected, query, "timestamp", null, null, true, true, true);
            });
        } finally {
            currentMicros = -1;
        }
    }

    @Test
    public void testNowPerformsBinarySearchOnTimestamp() throws Exception {
        try {
            currentMicros = 0;
            assertMemoryLeak(() -> {
                //create table
                // One hour step timestamps from epoch for 2000 steps
                final int count = 200;
                String createStmt = "create table xts as (select timestamp_sequence(0, 3600L * 1000 * 1000) ts from long_sequence(" + count + ")) timestamp(ts) partition by DAY";
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.000000Z'");
                compiler.compile(createStmt, sqlExecutionContext);

                formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                Stream<Object[]> dates = LongStream.rangeClosed(0, count - 1)
                        .map(i -> i * 3600L * 1000)
                        .mapToObj(ts -> new Object[]{ts * 1000L, formatter.format(new Date(ts))});

                List<Object[]> datesArr = dates.collect(Collectors.toList());

                final long hour = Timestamps.HOUR_MICROS;
                final long day = 24 * hour;
                compareNowRange("select * FROM xts WHERE ts >= '1970' and ts <= '2021'", datesArr, ts -> true, true);

                // Scroll now to the end
                currentMicros = 200L * hour;
                compareNowRange("select ts FROM xts WHERE ts >= now() - 3600 * 1000 * 1000L", datesArr, ts -> ts >= currentMicros - hour, true);
                compareNowRange("select ts FROM xts WHERE ts >= now() + 3600 * 1000 * 1000L", datesArr, ts -> ts >= currentMicros + hour, true);

                for (currentMicros = hour; currentMicros < count * hour; currentMicros += day) {
                    compareNowRange("select ts FROM xts WHERE ts < now()", datesArr, ts -> ts < currentMicros, true);
                }

                for (currentMicros = hour; currentMicros < count * hour; currentMicros += 12 * hour) {
                    compareNowRange("select ts FROM xts WHERE ts >= now()", datesArr, ts -> ts >= currentMicros, true);
                }

                for (currentMicros = 0; currentMicros < count * hour + 4 * day; currentMicros += 5 * hour) {
                    compareNowRange("select ts FROM xts WHERE ts <= dateadd('d', -1, now()) and ts >= dateadd('d', -2, now())",
                            datesArr,
                            ts -> ts >= (currentMicros - 2 * day) && (ts <= currentMicros - day), true);
                }

                currentMicros = 100L * hour;
                compareNowRange("WITH temp AS (SELECT ts FROM xts WHERE ts > dateadd('y', -1, now())) " +
                        "SELECT ts FROM temp WHERE ts < now()", datesArr, ts -> ts < currentMicros, true);
            });
        } finally {
            currentMicros = -1;
        }
    }

    @Test
    public void testNonContinuousPartitions() throws Exception {
        try {
            currentMicros = 0;
            assertMemoryLeak(() -> {
                // Create table
                // One hour step timestamps from epoch for 32 then skip 48 etc for 10 iterations
                final int count = 32;
                final int skip = 48;
                final int iterations = 10;
                final long hour = Timestamps.HOUR_MICROS;
                final long day = 24 * hour;

                String createStmt = "create table xts (ts Timestamp) timestamp(ts) partition by DAY";
                compiler.compile(createStmt, sqlExecutionContext);
                long start = 0;
                List<Object[]> datesArr = new ArrayList<>();
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.000000Z'");
                formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

                for (int i = 0; i < iterations; i++) {
                    String insert = "insert into xts " +
                            "select timestamp_sequence(" + start + "L, 3600L * 1000 * 1000) ts from long_sequence(" + count + ")";
                    compiler.compile(insert, sqlExecutionContext);
                    for (long ts = 0; ts < count; ts++) {
                        long nextTs = start + ts * hour;
                        datesArr.add(new Object[]{nextTs, formatter.format(nextTs / 1000L)});
                    }
                    start += (count + skip) * hour;
                }
                final long end = start;

                // Search with 3 hour window every 22 hours
                int min = Integer.MAX_VALUE;
                int max = Integer.MIN_VALUE;
                for (currentMicros = 0; currentMicros < end; currentMicros += 22 * hour) {
                    int results = compareNowRange("select ts FROM xts WHERE ts <= dateadd('h', 2, now()) and ts >= dateadd('h', -1, now())",
                            datesArr,
                            ts -> ts >= (currentMicros - hour) && (ts <= currentMicros + 2 * hour), true);
                    min = Math.min(min, results);
                    max = Math.max(max, results);
                }

                Assert.assertEquals(0, min);
                Assert.assertEquals(4, max);
            });
        } finally {
            currentMicros = -1;
        }
    }

    @Test
    public void testTimestampParseWithYearMonthDayTHourMinuteSecondAndIncompleteMillisTimeZone() throws Exception {
        assertMemoryLeak(() -> {
            //create table
            String createStmt = "create table ob_mem_snapshot (symbol int,  me_seq_num long,  timestamp timestamp) timestamp(timestamp) partition by DAY";
            compiler.compile(createStmt, sqlExecutionContext);
            //insert
            executeInsert("INSERT INTO ob_mem_snapshot  VALUES(1, 1, 1609459199000000)");
            String expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            String query = "select * from ob_mem_snapshot";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
            //2 millisec characters
            expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            query = "SELECT * FROM ob_mem_snapshot where timestamp = '2020-12-31T23:59:59.00Z'";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
            //1 millisec character
            expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            query = "SELECT * FROM ob_mem_snapshot where timestamp = '2020-12-31T23:59:59.0Z'";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
        });
    }

    @Test
    public void testTimestampParseWithYearMonthDayTHourMinuteSecondTimeZone() throws Exception {
        assertMemoryLeak(() -> {
            //create table
            String createStmt = "create table ob_mem_snapshot (symbol int,  me_seq_num long,  timestamp timestamp) timestamp(timestamp) partition by DAY";
            compiler.compile(createStmt, sqlExecutionContext);
            //insert
            executeInsert("INSERT INTO ob_mem_snapshot  VALUES(1, 1, 1609459199000000)");
            String expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            String query = "select * from ob_mem_snapshot";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
            expected = "symbol\tme_seq_num\ttimestamp\n" +
                    "1\t1\t2020-12-31T23:59:59.000000Z\n";
            query = "SELECT * FROM ob_mem_snapshot where timestamp ='2020-12-31T23:59:59Z'";
            printSqlResult(expected, query, "timestamp", null, null, true, true, true);
        });
    }

    private int compareNowRange(String query, List<Object[]> dates, LongPredicate filter, boolean expectSize) throws SqlException {
        String queryPlan = "{\"name\":\"DataFrameRecordCursorFactory\", \"cursorFactory\":{\"name\":\"IntervalFwdDataFrameCursorFactory\", \"table\":\"xts\"}}";
        long expectedCount = dates.stream().filter(arr -> filter.test((long) arr[0])).collect(Collectors.counting());
        String expected = "ts\n"
                + dates.stream().filter(arr -> filter.test((long) arr[0]))
                .map(arr -> arr[1] + "\n")
                .collect(Collectors.joining());
        printSqlResult(expected, query, "ts", null, null, true, true, expectSize, false, queryPlan);
        return (int) expectedCount;
    }
}
