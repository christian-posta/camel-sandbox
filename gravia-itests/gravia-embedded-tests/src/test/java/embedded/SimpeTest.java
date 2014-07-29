/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package embedded;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.gravia.runtime.*;
import org.jboss.gravia.runtime.Runtime;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * Created by ceposta
 * <a href="http://christianposta.com/blog>http://christianposta.com/blog</a>.
 */
@RunWith(Arquillian.class)
public class SimpeTest {

    static AtomicInteger callCount = new AtomicInteger();

    @BeforeClass
    public static void beforeClass() {
        assertEquals(1, callCount.incrementAndGet());
        Runtime runtime = RuntimeLocator.getRequiredRuntime();
        Set<Module> modules = runtime.getModules();
        assertEquals("Expected 7 modules" + modules, 7, modules.size());
    }



    @Test
    public void testHappyPath() {
        System.out.println("you did it");
    }
}
