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

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

/**
 * @author <a href="http://www.christianposta.com/blog">Christian Posta</a>
 */
public class OnExceptionTestWithDoTry extends CamelTestSupport{
    @Override
    public boolean isUseRouteBuilder() {
        return false;
    }


    @Test
    public void testBlaat() throws Exception {

        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                onException(Exception.class)
                        .to("mock:error")
                        .handled(true);

                from("direct:start")
                        .doTry()

                        .to("mock:exception")

                        .doCatch(Exception.class)
                        .to("mock:count")
                        .end();
            }
        });

//        context.getRouteDefinitions().get(0).adviceWith(context, new AdviceWithRouteBuilder() {
//            @Override
//            public void configure() throws Exception {
//                interceptSendToEndpoint("mock:exception")
//                        .skipSendToOriginalEndpoint()
//                        .process(new Processor() {
//                            @Override
//                            public void process(Exchange exchange) throws Exception {
//                                throw new Exception();
//                            }
//                        });
//
//            }
//        });

        context.start();


        getMockEndpoint("mock:count").expectedMessageCount(1);

        // alternate way to do this
        getMockEndpoint("mock:exception").whenAnyExchangeReceived(new Processor() {

            @Override
            public void process(Exchange exchange) throws Exception {
                throw new Exception("fail me");
            }
        });

        getMockEndpoint("mock:error").expectedMessageCount(0);

        template.sendBody("direct:start", "start");
        assertMockEndpointsSatisfied();
    }
}
