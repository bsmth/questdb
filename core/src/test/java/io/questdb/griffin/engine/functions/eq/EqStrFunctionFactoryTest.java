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

package io.questdb.griffin.engine.functions.eq;

import io.questdb.cairo.sql.RecordCursor;
import io.questdb.cairo.sql.RecordCursorFactory;
import io.questdb.griffin.AbstractGriffinTest;
import io.questdb.griffin.engine.functions.rnd.SharedRandom;
import io.questdb.std.Rnd;
import io.questdb.test.tools.TestUtils;
import org.junit.Before;
import org.junit.Test;

public class EqStrFunctionFactoryTest extends AbstractGriffinTest {

    @Before
    public void setUp3() {
        SharedRandom.RANDOM.set(new Rnd());
    }

    @Test
    public void testSimple() throws Exception {
        final String expected = "a\tb\tc\n" +
                "TO\tTO\t0.864511711022\n" +
                "JH\tJH\t0.293725743137\n" +
                "HR\tHR\t0.705236013489\n" +
                "GT\tGT\t0.320418516505\n" +
                "FF\tFF\t0.723774060553\n" +
                "HD\tHD\t0.921018327202\n" +
                "PG\tPG\t0.569301148082\n" +
                "NZ\tNZ\t0.750812468878\n" +
                "TK\tTK\t0.656634057464\n" +
                "FZ\tFZ\t0.700056044177\n" +
                "UW\tUW\t0.015101588517\n" +
                "HD\tHD\t0.379366696164\n" +
                "TR\tTR\t0.535853377530\n" +
                "CO\tCO\t0.007992908129\n" +
                "OS\tOS\t0.769345772543\n";

        TestUtils.assertMemoryLeak(() -> {
            compiler.compile("create table x as (" +
                    " select" +
                    " rnd_str(2,2,0) a," +
                    " rnd_str(2,2,0) b," +
                    " rnd_double(0) c" +
                    " from long_sequence(10000)" +
                    ")", sqlExecutionContext);

            try (RecordCursorFactory factory = compiler.compile("x where a = b", sqlExecutionContext).getRecordCursorFactory()) {
                sink.clear();

                try (RecordCursor cursor = factory.getCursor(sqlExecutionContext)) {
                    printer.print(cursor, factory.getMetadata(), true);
                }

                TestUtils.assertEquals(expected, sink);
            }

            engine.releaseAllReaders();
            engine.releaseAllWriters();
        });
    }

    @Test
    public void testStrEquals() throws Exception {
        final String expected = "a\tb\tc\n" +
                "\t\t0.736005795481\n" +
                "\t\t0.888454149250\n" +
                "\t\t0.507321975059\n" +
                "\t\t0.006428156810\n" +
                "\t\t0.195043256107\n" +
                "\t\t0.857872699497\n" +
                "\t\t0.374706013658\n" +
                "\t\t0.323410069144\n" +
                "\t\t0.051891241416\n" +
                "\t\t0.351737465333\n" +
                "\t\t0.610673817312\n" +
                "\t\t0.569218454032\n" +
                "\t\t0.658259990960\n" +
                "\t\t0.408452385193\n" +
                "\t\t0.990774115459\n" +
                "\t\t0.141847757100\n" +
                "\t\t0.916328143564\n" +
                "\t\t0.259121103066\n" +
                "\t\t0.262788521934\n" +
                "\t\t0.062029468049\n" +
                "\t\t0.644042004544\n" +
                "\t\t0.511174094681\n" +
                "\t\t0.644475642173\n" +
                "\t\t0.689548912727\n" +
                "\t\t0.416820310888\n" +
                "\t\t0.847242206369\n" +
                "\t\t0.671445193691\n" +
                "\t\t0.361372173908\n" +
                "\t\t0.329520695357\n";

        TestUtils.assertMemoryLeak(() -> {
            compiler.compile("create table x as (" +
                    " select" +
                    " rnd_str(4,4,8) a," +
                    " rnd_str(4,4,8) b," +
                    " rnd_double(0) c" +
                    " from long_sequence(10000)" +
                    ")", sqlExecutionContext);

            try (RecordCursorFactory factory = compiler.compile("x where a = b", sqlExecutionContext).getRecordCursorFactory()) {
                sink.clear();
                try (RecordCursor cursor = factory.getCursor(sqlExecutionContext)) {
                    printer.print(cursor, factory.getMetadata(), true);
                }

                TestUtils.assertEquals(expected, sink);
            }

            engine.releaseAllReaders();
            engine.releaseAllWriters();
        });
    }

    @Test
    public void testStrEqualsConstant() throws Exception {
        final String expected = "a\tb\tc\n" +
                "UW\tPV\t0.923691478032\n" +
                "UW\tEW\t0.046158585511\n" +
                "UW\tLP\t0.812164456445\n" +
                "UW\tMD\t0.743096200973\n" +
                "UW\tWN\t0.971829797336\n" +
                "UW\tPQ\t0.900435046194\n" +
                "UW\tJS\t0.029348048369\n" +
                "UW\tEJ\t0.085101480777\n" +
                "UW\tKZ\t0.666129919823\n" +
                "UW\tWX\t0.552643516151\n" +
                "UW\tHQ\t0.529423895223\n" +
                "UW\tUW\t0.015101588517\n" +
                "UW\tMK\t0.964233343466\n";

        TestUtils.assertMemoryLeak(() -> {
            compiler.compile("create table x as (" +
                    " select" +
                    " rnd_str(2,2,0) a," +
                    " rnd_str(2,2,0) b," +
                    " rnd_double(0) c" +
                    " from long_sequence(10000)" +
                    ")", sqlExecutionContext);

            try (RecordCursorFactory factory = compiler.compile("x where a = 'UW'", sqlExecutionContext).getRecordCursorFactory()) {
                sink.clear();

                try (RecordCursor cursor = factory.getCursor(sqlExecutionContext)) {
                    printer.print(cursor, factory.getMetadata(), true);
                }

                TestUtils.assertEquals(expected, sink);
            }

            engine.releaseAllReaders();
            engine.releaseAllWriters();
        });
    }

    @Test
    public void testStrEqualsConstant2() throws Exception {
        final String expected = "a\tb\tc\n" +
                "UW\tPV\t0.923691478032\n" +
                "UW\tEW\t0.046158585511\n" +
                "UW\tLP\t0.812164456445\n" +
                "UW\tMD\t0.743096200973\n" +
                "UW\tWN\t0.971829797336\n" +
                "UW\tPQ\t0.900435046194\n" +
                "UW\tJS\t0.029348048369\n" +
                "UW\tEJ\t0.085101480777\n" +
                "UW\tKZ\t0.666129919823\n" +
                "UW\tWX\t0.552643516151\n" +
                "UW\tHQ\t0.529423895223\n" +
                "UW\tUW\t0.015101588517\n" +
                "UW\tMK\t0.964233343466\n";

        TestUtils.assertMemoryLeak(() -> {
            compiler.compile("create table x as (" +
                    " select" +
                    " rnd_str(2,2,0) a," +
                    " rnd_str(2,2,0) b," +
                    " rnd_double(0) c" +
                    " from long_sequence(10000)" +
                    ")", sqlExecutionContext);

            try (RecordCursorFactory factory = compiler.compile("x where 'UW' = a", sqlExecutionContext).getRecordCursorFactory()) {
                sink.clear();

                try (RecordCursor cursor = factory.getCursor(sqlExecutionContext)) {
                    printer.print(cursor, factory.getMetadata(), true);
                }

                TestUtils.assertEquals(expected, sink);
            }

            engine.releaseAllReaders();
            engine.releaseAllWriters();
        });
    }

    @Test
    public void testStrEqualsNull() throws Exception {
        final String expected = "a\tb\tc\n" +
                "\tSY\t0.962283803859\n" +
                "\tZU\t0.948685205706\n" +
                "\tMG\t0.591453611066\n" +
                "\tHX\t0.438165062627\n" +
                "\tLG\t0.080922888665\n" +
                "\tGF\t0.946229102187\n" +
                "\tXW\t0.306393180274\n" +
                "\tYE\t0.467723696273\n" +
                "\tGP\t0.428403698439\n" +
                "\tLX\t0.971586265072\n" +
                "\tRB\t0.561342235360\n" +
                "\tPD\t0.850102613275\n";

        TestUtils.assertMemoryLeak(() -> {
            compiler.compile("create table x as (" +
                    " select" +
                    " rnd_str(2,2,400) a," +
                    " rnd_str(2,2,0) b," +
                    " rnd_double(0) c" +
                    " from long_sequence(10000)" +
                    ")", sqlExecutionContext);

            try (RecordCursorFactory factory = compiler.compile("x where a = null", sqlExecutionContext).getRecordCursorFactory()) {
                sink.clear();

                try (RecordCursor cursor = factory.getCursor(sqlExecutionContext)) {
                    printer.print(cursor, factory.getMetadata(), true);
                }

                TestUtils.assertEquals(expected, sink);
            }

            engine.releaseAllReaders();
            engine.releaseAllWriters();
        });
    }
}
