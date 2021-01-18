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

package io.questdb.cutlass.http;

import io.questdb.log.Log;
import io.questdb.log.LogFactory;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ImportIODispatcherTest {
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();
    private static final Log LOG = LogFactory.getLog(ImportIODispatcherTest.class);

    private static final String ValidImportRequest1 = "POST /upload?name=trips HTTP/1.1\r\n" +
            "Host: localhost:9001\r\n" +
            "User-Agent: curl/7.64.0\r\n" +
            "Accept: */*\r\n" +
            "Content-Length: 437760673\r\n" +
            "Content-Type: multipart/form-data; boundary=------------------------27d997ca93d2689d\r\n" +
            "Expect: 100-continue\r\n" +
            "\r\n" +
            "--------------------------27d997ca93d2689d\r\n" +
            "Content-Disposition: form-data; name=\"schema\"; filename=\"schema.json\"\r\n" +
            "Content-Type: application/octet-stream\r\n" +
            "\r\n" +
            "[\r\n" +
            "  {\r\n" +
            "    \"name\": \"DispatchingBaseNum\",\r\n" +
            "    \"type\": \"STRING\"\r\n" +
            "  },\r\n" +
            "  {\r\n" +
            "    \"name\": \"PickupDateTime\",\r\n" +
            "    \"type\": \"TIMESTAMP\",\r\n" +
            "    \"pattern\": \"yyyy-MM-dd HH:mm:ss\"\r\n" +
            "  }\r\n" +
            "]\r\n" +
            "\r\n" +
            "--------------------------27d997ca93d2689d\r\n" +
            "Content-Disposition: form-data; name=\"data\"; filename=\"fhv_tripdata_2017-02.csv\"\r\n" +
            "Content-Type: application/octet-stream\r\n" +
            "\r\n" +
            "Dispatching_base_num,Pickup_DateTime,DropOff_datetime\r\n" +
            "B00008,2017-02-01 00:30:00,\r\n" +
            "B00008,2017-02-01 00:40:00,\r\n" +
            "B00009,2017-02-01 00:50:00,\r\n" +
            "B00013,2017-02-01 00:51:00,\r\n" +
            "B00013,2017-02-01 01:41:00,\r\n" +
            "B00013,2017-02-01 02:00:00,\r\n" +
            "B00013,2017-02-01 03:53:00,\r\n" +
            "B00013,2017-02-01 04:44:00,\r\n" +
            "B00013,2017-02-01 05:05:00,\r\n" +
            "B00013,2017-02-01 06:54:00,\r\n" +
            "B00014,2017-02-01 07:45:00,\r\n" +
            "B00014,2017-02-01 08:45:00,\r\n" +
            "B00014,2017-02-01 09:46:00,\r\n" +
            "B00014,2017-02-01 10:54:00,\r\n" +
            "B00014,2017-02-01 11:45:00,\r\n" +
            "B00014,2017-02-01 11:45:00,\r\n" +
            "B00014,2017-02-01 11:45:00,\r\n" +
            "B00014,2017-02-01 12:26:00,\r\n" +
            "B00014,2017-02-01 12:55:00,\r\n" +
            "B00014,2017-02-01 13:47:00,\r\n" +
            "B00014,2017-02-01 14:05:00,\r\n" +
            "B00014,2017-02-01 14:58:00,\r\n" +
            "B00014,2017-02-01 15:33:00,\r\n" +
            "B00014,2017-02-01 15:45:00,\r\n" +
            "\r\n" +
            "--------------------------27d997ca93d2689d--";

    private static final String ValidImportRequest2 = "POST /upload?name=trips HTTP/1.1\r\n" +
            "Host: localhost:9001\r\n" +
            "User-Agent: curl/7.64.0\r\n" +
            "Accept: */*\r\n" +
            "Content-Length: 437760673\r\n" +
            "Content-Type: multipart/form-data; boundary=------------------------27d997ca93d2689d\r\n" +
            "Expect: 100-continue\r\n" +
            "\r\n" +
            "--------------------------27d997ca93d2689d\r\n" +
            "Content-Disposition: form-data; name=\"schema\"; filename=\"schema.json\"\r\n" +
            "Content-Type: application/octet-stream\r\n" +
            "\r\n" +
            "[\r\n" +
            "  {\r\n" +
            "    \"name\": \"Col1\",\r\n" +
            "    \"type\": \"STRING\"\r\n" +
            "  },\r\n" +
            "  {\r\n" +
            "    \"name\": \"Col2\",\r\n" +
            "    \"type\": \"STRING\"\r\n" +
            "  },\r\n" +
            "  {\r\n" +
            "    \"name\": \"Col3\",\r\n" +
            "    \"type\": \"STRING\"\r\n" +
            "  },\r\n" +
            "  {\r\n" +
            "    \"name\": \"Col4\",\r\n" +
            "    \"type\": \"STRING\"\r\n" +
            "  },\r\n" +
            "  {\r\n" +
            "    \"name\": \"PickupDateTime\",\r\n" +
            "    \"type\": \"TIMESTAMP\",\r\n" +
            "    \"pattern\": \"yyyy-MM-dd HH:mm:ss\"\r\n" +
            "  }\r\n" +
            "]\r\n" +
            "\r\n" +
            "--------------------------27d997ca93d2689d\r\n" +
            "Content-Disposition: form-data; name=\"data\"; filename=\"table2.csv\"\r\n" +
            "Content-Type: application/octet-stream\r\n" +
            "\r\n" +
            "Co1,Col2,Col3,Col4,PickupDateTime\r\n" +
            "B00008,,,,2017-02-01 00:30:00\r\n" +
            "B00008,,,,2017-02-01 00:40:00\r\n" +
            "B00009,,,,2017-02-01 00:50:00\r\n" +
            "B00013,,,,2017-02-01 00:51:00\r\n" +
            "B00013,,,,2017-02-01 01:41:00\r\n" +
            "B00013,,,,2017-02-01 02:00:00\r\n" +
            "B00013,,,,2017-02-01 03:53:00\r\n" +
            "B00013,,,,2017-02-01 04:44:00\r\n" +
            "B00013,,,,2017-02-01 05:05:00\r\n" +
            "B00013,,,,2017-02-01 06:54:00\r\n" +
            "B00014,,,,2017-02-01 07:45:00\r\n" +
            "B00014,,,,2017-02-01 08:45:00\r\n" +
            "B00014,,,,2017-02-01 09:46:00\r\n" +
            "B00014,,,,2017-02-01 10:54:00\r\n" +
            "B00014,,,,2017-02-01 11:45:00\r\n" +
            "B00014,,,,2017-02-01 11:45:00\r\n" +
            "B00014,,,,2017-02-01 11:45:00\r\n" +
            "B00014,,,,2017-02-01 12:26:00\r\n" +
            "B00014,,,,2017-02-01 12:55:00\r\n" +
            "B00014,,,,2017-02-01 13:47:00\r\n" +
            "B00014,,,,2017-02-01 14:05:00\r\n" +
            "B00014,,,,2017-02-01 14:58:00\r\n" +
            "B00014,,,,2017-02-01 15:33:00\r\n" +
            "B00014,,,,2017-02-01 15:45:00\r\n" +
            "\r\n" +
            "--------------------------27d997ca93d2689d--";

    private final String ValidImportResponse1 = "HTTP/1.1 200 OK\r\n" +
            "Server: questDB/1.0\r\n" +
            "Date: Thu, 1 Jan 1970 00:00:00 GMT\r\n" +
            "Transfer-Encoding: chunked\r\n" +
            "Content-Type: text/plain; charset=utf-8\r\n" +
            "\r\n" +
            "04f1\r\n" +
            "+---------------------------------------------------------------------------------------------------------------+\r\n" +
            "|      Location:  |                                             trips  |        Pattern  | Locale  |    Errors  |\r\n" +
            "|   Partition by  |                                              NONE  |                 |         |            |\r\n" +
            "+---------------------------------------------------------------------------------------------------------------+\r\n" +
            "|   Rows handled  |                                                24  |                 |         |            |\r\n" +
            "|  Rows imported  |                                                24  |                 |         |            |\r\n" +
            "+---------------------------------------------------------------------------------------------------------------+\r\n" +
            "|              0  |                                DispatchingBaseNum  |                   STRING  |         0  |\r\n" +
            "|              1  |                                    PickupDateTime  |                TIMESTAMP  |         0  |\r\n" +
            "|              2  |                                   DropOffDatetime  |                   STRING  |         0  |\r\n" +
            "+---------------------------------------------------------------------------------------------------------------+\r\n" +
            "\r\n" +
            "00\r\n" +
            "\r\n";

    private final String ValidImportResponse2 = "HTTP/1.1 200 OK\r\n" +
            "Server: questDB/1.0\r\n" +
            "Date: Thu, 1 Jan 1970 00:00:00 GMT\r\n" +
            "Transfer-Encoding: chunked\r\n" +
            "Content-Type: text/plain; charset=utf-8\r\n" +
            "\r\n" +
            "05d7\r\n" +
            "+---------------------------------------------------------------------------------------------------------------+\r\n" +
            "|      Location:  |                                             trips  |        Pattern  | Locale  |    Errors  |\r\n" +
            "|   Partition by  |                                              NONE  |                 |         |            |\r\n" +
            "+---------------------------------------------------------------------------------------------------------------+\r\n" +
            "|   Rows handled  |                                                24  |                 |         |            |\r\n" +
            "|  Rows imported  |                                                24  |                 |         |            |\r\n" +
            "+---------------------------------------------------------------------------------------------------------------+\r\n" +
            "|              0  |                                              Col1  |                   STRING  |         0  |\r\n" +
            "|              1  |                                              Col2  |                   STRING  |         0  |\r\n" +
            "|              2  |                                              Col3  |                   STRING  |         0  |\r\n" +
            "|              3  |                                              Col4  |                   STRING  |         0  |\r\n" +
            "|              4  |                                    PickupDateTime  |                TIMESTAMP  |         0  |\r\n" +
            "+---------------------------------------------------------------------------------------------------------------+\r\n" +
            "\r\n" +
            "00\r\n" +
            "\r\n";

    private final String DdlCols1 = "(DispatchingBaseNum+STRING,PickupDateTime+TIMESTAMP,DropOffDatetime+STRING)";
    private final String DdlCols2 = "(Col1+STRING,Col2+STRING,Col3+STRING,Col4+STRING,PickupDateTime+TIMESTAMP)+timestamp(PickupDateTime)";

    @Test
    public void testImportWithWrongTimestampSpecifiedLoop() throws Exception {
        for (int i = 0; i < 5; i++) {
            System.out.println("*************************************************************************************");
            System.out.println("**************************         Run " + i + "            ********************************");
            System.out.println("*************************************************************************************");
            testImportWithWrongTimestampSpecified();
            temp.delete();
            temp.create();
        }
    }

    private void testImportWithWrongTimestampSpecified() throws Exception {
        final int parallelCount = 2;
        final int insertCount = 9;
        new HttpQueryTestBuilder()
                .withTempFolder(temp)
                .withWorkerCount(parallelCount)
                .withHttpServerConfigBuilder(new HttpServerConfigurationBuilder())
                .withTelemetry(false)
                .run((engine) -> {
                    CountDownLatch countDownLatch = new CountDownLatch(parallelCount);
                    AtomicInteger success = new AtomicInteger();
                    String[] reqeusts = new String[] {ValidImportRequest1, ValidImportRequest2};
                    String[] response = new String[] {ValidImportResponse1, ValidImportResponse2};
                    String[] ddl = new String[] {DdlCols1, DdlCols2};

                    for (int i = 0; i < parallelCount; i++) {
                        final int thread = i;
                        final String respTemplate = response[i];
                        final String requestTemplate = reqeusts[i];
                        final String ddlCols = ddl[i];
                        final String tableName = "trip" + i;

                        new SendAndReceiveRequestBuilder().executeWithStandardHeaders(
                                "GET /query?query=CREATE+TABLE+" + tableName + ddlCols + "; HTTP/1.1\r\n",
                                "0c\r\n" +
                                        "{\"ddl\":\"OK\"}\r\n" +
                                        "00\r\n" +
                                        "\r\n");

                        new Thread(() -> {
                            try {
                                for (int r = 0; r < insertCount; r++) {
                                    try {
                                        String timestamp= "";
                                        if (r > 0 && thread > 0) {
                                            timestamp = "&timestamp=PickupDateTime";
                                        }
                                        String request = requestTemplate
                                                .replace("POST /upload?name=trips HTTP", "POST /upload?name=" + tableName + timestamp + " HTTP")
                                                .replace("2017-02-01", "2017-02-0" + (r + 1));

                                        new SendAndReceiveRequestBuilder().execute(request, respTemplate.replace("trips", tableName));
                                        success.incrementAndGet();
                                    } catch (Exception e) {
                                        LOG.error().$("Failed execute insert http request. Server error ").$(e).$();
                                    }
                                }
                            } finally {
                                countDownLatch.countDown();
                            }
                        }).start();
                    }

                    final int totalImports = parallelCount * insertCount;
                    boolean finished = countDownLatch.await(200 * totalImports, TimeUnit.MILLISECONDS);
                    Assert.assertTrue(
                            "Import is not finished in reasonable time, check server errors",
                            finished);
                    Assert.assertEquals(
                            "Expected successful import count does not match actual imports",
                            totalImports,
                            success.get());
                });
    }
}
