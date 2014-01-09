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
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * @author <a href="http://www.christianposta.com/blog">Christian Posta</a>
 */
public class AvroTest {

    @Before
    public void setup() {
        File avroTarget = new File("target/avro");
        if (!avroTarget.exists()) {
            avroTarget.mkdirs();
        }
    }

    @Test
    public void testAvroSerializeDeserialize() throws IOException {
        File schemaFile = new File("src/test/resources/avro/user.avsc");
        assertTrue(schemaFile.exists());
        System.out.println("Path: " + schemaFile.getAbsoluteFile());

        Schema schema = new Schema.Parser().parse(schemaFile);

        GenericRecord user1 = new GenericData.Record(schema);
        user1.put("name", "Alyssa");
        user1.put("favorite_number", 256);

        GenericRecord user2 = new GenericData.Record(schema);
        user2.put("name", "Ben");
        user2.put("favorite_number", 7);
        user2.put("favorite_color", "red");

        File file = new File("target/avro/users.avro");
        if (file.exists()) {
            System.out.println("We are deleting this file since we found an older version...");
            file.delete();
        }

        DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<GenericRecord>(schema);
        DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<GenericRecord>(datumWriter);
        dataFileWriter.create(schema, file);
        dataFileWriter.append(user1);
        dataFileWriter.append(user2);
        dataFileWriter.close();

        assertTrue(new File("target/avro/users.avro").exists());
    }
}
