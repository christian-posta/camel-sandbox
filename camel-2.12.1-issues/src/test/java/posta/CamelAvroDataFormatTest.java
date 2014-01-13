/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package posta;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.avro.AvroDataFormat;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * @author <a href="http://www.christianposta.com/blog">Christian Posta</a>
 */
public class CamelAvroDataFormatTest extends CamelTestSupport{

    private static Schema schema;

    @BeforeClass
    public static void setUpSchema() throws IOException {
        File schemaFile = new File("src/test/resources/avro/user.avsc");
        assertTrue(schemaFile.exists());
        System.out.println("Path: " + schemaFile.getAbsoluteFile());

        schema = new Schema.Parser().parse(schemaFile);
    }

    @Test
    public void testUnmarshal() throws IOException {
        GenericRecord user1 = new GenericData.Record(schema);
        user1.put("name", "Alyssa");
        user1.put("favorite_number", 256);

        DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<GenericRecord>(schema);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
        datumWriter.write(user1, encoder);
        encoder.flush();

        template.sendBody("direct:back", outputStream);

    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                AvroDataFormat format = new AvroDataFormat(schema);

                from("direct:back").unmarshal(format).to("mock:reverse");

            }
        };
    }
}
