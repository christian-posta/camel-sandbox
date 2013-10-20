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
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class ErrorTest extends CamelTestSupport {

    @Test
    public void testErrorIsThrown() throws Exception {

        getMockEndpoint("mock:foundException").expectedMessageCount(1);
        getMockEndpoint("mock:never").expectedMessageCount(3);

        getMockEndpoint("mock:never").whenAnyExchangeReceived(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                throw new RuntimeException();
            }
        });

        try {
            template.sendBody("direct:start", "test");
            fail("Should propagate error and not reach here");
        } catch (Exception e) {
            System.out.println("Caught exception");
        }
        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {


                onException(Exception.class).to("mock:foundException").end();

                from("direct:start")
                        .to("direct:internal");

                from("direct:internal").id("2")
                        .errorHandler(defaultErrorHandler().maximumRedeliveries(2))
                        .to("mock:never");
            }
        };
    }
}