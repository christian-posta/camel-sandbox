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

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.processor.aggregate.UseLatestAggregationStrategy;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import javax.jms.*;

/**
 * @author <a href="http://www.christianposta.com/blog">Christian Posta</a>
 */
public class ActiveMQOnCompletionTest extends CamelTestSupport {

    @Test
    public void testOnCompletion() throws InterruptedException, JMSException {

        getMockEndpoint("mock:completed").expectedMessageCount(1);
        getMockEndpoint("mock:preaggregate").expectedMessageCount(3);
        getMockEndpoint("mock:end").expectedMessageCount(1);

        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("vm://localhost?create=false");

        Connection connection = factory.createConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createQueue("incoming");
        MessageProducer producer = session.createProducer(destination);


        // send 3 messages
        for (int i = 0; i < 3; i++) {
            TextMessage message = session.createTextMessage("body=" + i);
            message.setStringProperty("user_id", "1007");
            producer.send(message);
        }

        producer.close();
        session.close();
        connection.close();

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {


                from("activemq:incoming").routeId("myRouteID")
                        .onCompletion().onCompleteOnly().log("completed w/ body ${body}").to("mock:completed").end()
                        .log("header for user is ${header.user_id}")
                        .choice()
                            .when(header("VALID_MSG").isEqualTo("false"))
                                .log(" Error")
                            .otherwise()
                                .to("mock:preaggregate")
                                .aggregate(header("user_id"), new UseLatestAggregationStrategy()).completionTimeout(2000)
                                    .to("mock:end")
                        .end();
            }
        };
    }

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry rc = super.createRegistry();
        ActiveMQComponent activeMQComponent = ActiveMQComponent.activeMQComponent("vm://localhost");
        rc.bind("activemq", activeMQComponent);
        return rc;
    }
}
