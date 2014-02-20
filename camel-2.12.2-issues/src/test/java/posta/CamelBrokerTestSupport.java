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
package posta;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.camel.test.junit4.CamelTestSupport;

/**
 * @author <a href="http://www.christianposta.com/blog">Christian Posta</a>
 */
public abstract class CamelBrokerTestSupport extends CamelTestSupport {

    protected BrokerService broker;
    protected ActiveMQConnectionFactory connectionFactory;

    protected abstract void configureBroker(BrokerService broker);

    @Override
    public void setUp() throws Exception {
        createBroker();
        startBroker();
        createConnectionFactory();
        super.setUp();
    }

    private void createConnectionFactory() {
        this.connectionFactory = new ActiveMQConnectionFactory("vm://localhost");
    }

    private void startBroker() throws Exception {
        assertNotNull(broker);
        broker.start();
        broker.waitUntilStarted();
    }

    private void createBroker() {
        this.broker = new BrokerService();
        configureBroker(broker);
    }

    @Override
    public void tearDown() throws Exception {

        broker.stop();
        broker.waitUntilStopped();
        broker = null;
        super.tearDown();
    }
}
