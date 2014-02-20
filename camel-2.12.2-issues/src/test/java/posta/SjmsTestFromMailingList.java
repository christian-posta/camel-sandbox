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
import org.apache.activemq.broker.jmx.DestinationViewMBean;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.sjms.SjmsComponent;
import org.apache.camel.impl.JndiRegistry;
import org.junit.Test;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="http://www.christianposta.com/blog">Christian Posta</a>
 */
public class SjmsTestFromMailingList extends CamelBrokerTestSupport {



    @Test
    public void testSjmsTTL() throws Exception {
        template.sendBody("jms:testQ1", "hello, world");
        template.sendBody("sjms:testQ2?ttl=2000", "goodbye, world");

        // sleep for a bit to make sure messages got expired
        TimeUnit.SECONDS.sleep(5);

        assertNoMessagesInQueue("testQ1");
        assertNoMessagesInQueue("testQ2");


    }

    private void assertNoMessagesInQueue(String queueName) throws MalformedObjectNameException {
        DestinationViewMBean destinationView = createDestinationView(queueName);
        assertEquals("There are messages left in " + queueName, 0, destinationView.getQueueSize());
    }

    private DestinationViewMBean createDestinationView(String queueName) throws MalformedObjectNameException {
        ObjectName name = new ObjectName("org.apache.activemq"
                + ":type=Broker,brokerName=localhost,destinationType=Queue,destinationName="
                + queueName);

        return (DestinationViewMBean) broker.getManagementContext().newProxyInstance(name, DestinationViewMBean.class, true);
    }


    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry rc = super.createRegistry();
        rc.bind("sjms", createSjmsComponent());
        rc.bind("jms", createJmsComponent());
        return rc;
    }

    private Object createSjmsComponent() {
        SjmsComponent rc = new SjmsComponent();
        rc.setConnectionFactory(connectionFactory);
        return rc;
    }

    private Object createJmsComponent() {
        JmsComponent rc = JmsComponent.jmsComponent(connectionFactory);
        rc.setTimeToLive(2000);
        return rc;
    }




    protected void configureBroker(BrokerService broker) {
        broker.setBrokerName("localhost");
        broker.setDeleteAllMessagesOnStartup(true);

        ActiveMQQueue queueName = new ActiveMQQueue(">");
        PolicyEntry entry = new PolicyEntry();
        entry.setDestination(queueName);
        entry.setExpireMessagesPeriod(1000);

        PolicyMap policyMap = new PolicyMap();
        policyMap.put(queueName, entry);
        broker.setDestinationPolicy(policyMap);
    }


}
