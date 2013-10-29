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
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.language.ognl.OgnlLanguage;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import static org.apache.camel.ExchangePattern.InOut;

/**
 * @author <a href="http://www.christianposta.com/blog">Christian Posta</a>
 */
public class NestedJmsReqReplyTest extends CamelTestSupport {

    @Test
    public void testFromMailingList() throws InterruptedException {
        MockEndpoint mockEndpoint = getMockEndpoint("mock:end");
        mockEndpoint.expectedBodiesReceived("you win");

        template.sendBody("direct:in", "hi");

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {

                from("direct:in").setExchangePattern(InOut)
                        .log("start first send")
                        .to("activemq:request1?replyTo=responseQueue&jmsMessageType=Text")
                        .log("received final response ${body}")
                        .to("mock:end");

                from("activemq:request1?useMessageIDAsCorrelationID=false")
                        .log("sending ${body}")
                        .to("log:" + NestedJmsReqReplyTest.class + ".request1?showAll=true")
                        .inOut("activemq:request2?jmsMessageType=Text");

                from("activemq:request2")
                        .log("about to xform ${body}")
                        .to("log:" + NestedJmsReqReplyTest.class + "?showAll=true")
                        .transform(constant("you win"))
                        .log("returning constant ${body}");
            }
        };
    }

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry rc = super.createRegistry();
        rc.bind("activemq", ActiveMQComponent.jmsComponent(new ActiveMQConnectionFactory("vm://local-test")));
        return rc;
    }
}
