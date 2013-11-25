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

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="http://www.christianposta.com/blog">Christian Posta</a>
 */
public class FilterAggregatorTest extends CamelTestSupport{

    @Test
    public void testFile() throws InterruptedException {
        List<String> file = createFile();
        template.sendBody("seda:in", file);

        MockEndpoint mockEndpoint = getMockEndpoint("mock:end");
        mockEndpoint.expectedMessageCount(1);

        mockEndpoint.message(0).body().equals("HEADER RECORD RECORD RECORD");
        assertMockEndpointsSatisfied();
    }

    private List<String> createFile() {
        LinkedList<String> lines = new LinkedList<String>();
        lines.add("HEADER");
        lines.add("RECORD");
        lines.add("RECORD");
        lines.add("RECORD");
        lines.add("FOOT");
        return lines;
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from( "seda:in").routeId("posta.FilterAggregatorTest.in")
                        .onException(Exception.class).maximumRedeliveries(0)
                            .log( "Error processing file")
                        .end()
                        .split(body()).streaming().stopOnException()
                            .filter(method(HeaderFooterCheck.class))
                            .aggregate(constant(true), new Aggregator()).completionInterval(500).to("mock:end")
                        .end()
                        .end();
            }
        };
    }

    static class Aggregator implements AggregationStrategy {

        @Override
        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
            if (oldExchange == null) {
                return newExchange;
            }

            // process anything that is not a footer
            String newBody = newExchange.getIn().getBody(String.class);
            if (!(newBody.equals("FOOT"))) {
                String oldBody = oldExchange.getIn().getBody(String.class);
                oldBody = oldBody + " " + newBody;
                oldExchange.getIn().setBody(oldBody);
            }

            return oldExchange;
        }
    }

    public static class HeaderFooterCheck {

        public boolean shouldAllow(@Body String line) {
            return !"FOOT".equalsIgnoreCase(line);
        }
    }
}
