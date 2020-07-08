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

package io.questdb;

import io.questdb.cairo.*;
import io.questdb.std.*;
import io.questdb.std.str.Path;
import io.questdb.test.tools.*;

import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

public class TelemetryTest extends AbstractCairoTest {
    private final static FilesFacade FF = FilesFacadeImpl.INSTANCE;

    @Test
    public void testTelemetryDisabledByDefault() throws Exception {
        TestUtils.assertMemoryLeak(() -> {
            try (Path path = new Path()) {
                Assert.assertEquals(TableUtils.TABLE_DOES_NOT_EXIST, TableUtils.exists(FF, path, root, "telemetry"));
                Assert.assertEquals(TableUtils.TABLE_DOES_NOT_EXIST, TableUtils.exists(FF, path, root, "telemetry_config"));
            }
        });
    }

    @Test
    public void testTelemetryCreatesTablesWhenEnabled() throws Exception {
        TestUtils.assertMemoryLeak(() -> {
            try (CairoEngine engine = new CairoEngine(configuration, null)) {
                final TelemetryJob telemetryJob = new TelemetryJob(serverConfiguration, engine, messageBus, null);

                try (Path path = new Path()) {
                    Assert.assertEquals(TableUtils.TABLE_EXISTS, TableUtils.exists(FF, path, root, "telemetry"));
                    Assert.assertEquals(TableUtils.TABLE_EXISTS, TableUtils.exists(FF, path, root, "telemetry_config"));
                }

                Misc.free(telemetryJob);
            }
        });
    }

    @Test
    public void testTelemetryStoresUpAndDownEvents() throws Exception {
        TestUtils.assertMemoryLeak(() -> {
            serverConfiguration = new PropServerConfiguration(temp.getRoot().getAbsolutePath(), new Properties()) {
                @Override
                public CairoConfiguration getCairoConfiguration() {
                    return configuration;
                }
            };
            configuration = serverConfiguration.getCairoConfiguration();
            messageBus = new MessageBusImpl(serverConfiguration);
            CairoEngine engine = new CairoEngine(configuration, messageBus);
            TelemetryJob telemetryJob = new TelemetryJob(serverConfiguration, engine, messageBus, null);
            Misc.free(telemetryJob);

            final String expectedEvent = "100\n" +
                    "101\n";
            assertColumn(expectedEvent, "telemetry", 1);

            final String expectedOrigin = "1\n" +
                    "1\n";
            assertColumn(expectedOrigin, "telemetry", 2);

            Misc.free(engine);
        });
    }
}
