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

import org.apache.activemq.broker.BrokerService;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.junit.Test;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="http://www.christianposta.com/blog">Christian Posta</a>
 */
public class SplittingWithinJmsTransaction extends CamelBrokerTestSupport {

    @Test
    public void testSplitWithinTransaction() throws InterruptedException {
        template.sendBody("jms:csv-request", "hello world");

        // sleep for a bit to see if things work. can change to notify builder
        // later
        TimeUnit.SECONDS.sleep(5);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("jms:queue:csv-request?maxConcurrentConsumers=1")
                        .transacted("propogationRequired")
                        .process(new UriToFileProcessor())
                        .split().tokenize("\n", 1)
                            .streaming()
                            .parallelProcessing()
                            .stopOnException()
                            .shareUnitOfWork()
                            .choice()
                                    .when().simple("${property.CamelSplitIndex} > 1")
                                    .inOnly("jms:queue:prepare-order")
                                .otherwise()
                                    .log("Header: ${body}")
                            .endChoice()
                        .end();

                from("jms:queue:prepare-order").log("got a message: ${body}");
            }
        };
    }

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry rc = super.createRegistry();
        JmsComponent jmsComponent = createJmsComponent();
        PlatformTransactionManager transactionManager = jmsComponent.getConfiguration().getTransactionManager();
        rc.bind("jms", createJmsComponent());
        rc.bind("propogationRequired", createPropogationRequiredPolicy(transactionManager));
        return rc;
    }

    private Object createPropogationRequiredPolicy(PlatformTransactionManager transactionManager) {
        SpringTransactionPolicy policy = new SpringTransactionPolicy();
        policy.setTransactionManager(transactionManager);
        policy.setPropagationBehaviorName("PROPAGATION_REQUIRED");
        return policy;
    }

    private JmsComponent createJmsComponent() {
        JmsComponent rc = JmsComponent.jmsComponentTransacted(connectionFactory);
        return rc;
    }

    @Override
    protected void configureBroker(BrokerService broker) {

    }
}
