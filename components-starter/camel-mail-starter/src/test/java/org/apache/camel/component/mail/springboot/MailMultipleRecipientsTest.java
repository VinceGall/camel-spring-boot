/*
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
package org.apache.camel.component.mail.springboot;



import java.util.HashMap;
import java.util.Map;


import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;

import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.boot.CamelAutoConfiguration;

import org.junit.jupiter.api.Test;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.jvnet.mock_javamail.Mailbox;


@DirtiesContext
@CamelSpringBootTest
@SpringBootTest(
    classes = {
        CamelAutoConfiguration.class,
        MailMultipleRecipientsTest.class,
        MailMultipleRecipientsTest.TestConfiguration.class
    }
)
public class MailMultipleRecipientsTest {

    
    @Autowired
    ProducerTemplate template;
    
    @Autowired
    CamelContext context;
    
    @EndpointInject("mock:claus")
    MockEndpoint mockClaus;
    
    @EndpointInject("mock:willem")
    MockEndpoint mockWillem;
    
    @EndpointInject("mock:hadrian")
    MockEndpoint mockHadrian;
    
    @EndpointInject("mock:tracy")
    MockEndpoint mockTracy;
    
    
    @Test
    public void testSendWithMultipleRecipientsInHeader() throws Exception {
        Mailbox.clearAll();

        // START SNIPPET: e1
        Map<String, Object> headers = new HashMap<>();
        // test with both comma and semi colon as Camel supports both kind of separators
        headers.put("to", "claus@localhost, willem@localhost ; hadrian@localhost, \"Snell, Tracy\" <tracy@localhost>");
        headers.put("cc", "james@localhost");

        assertMailbox(mockClaus);
        assertMailbox(mockWillem);
        assertMailbox(mockHadrian);
        assertMailbox(mockTracy);

        template.sendBodyAndHeaders("smtp://localhost", "Hello World", headers);
        // END SNIPPET: e1

        MockEndpoint.assertIsSatisfied(context);
    }

    @Test
    public void testSendWithMultipleRecipientsPreConfigured() throws Exception {
        Mailbox.clearAll();

        assertMailbox(mockClaus);
        assertMailbox(mockWillem);

        // START SNIPPET: e2
        // here we have pre configured the to receivers to claus and willem. Notice we use comma to separate
        // the two recipients. Camel also support using colon as separator char
        template.sendBody("smtp://localhost?to=claus@localhost,willem@localhost&cc=james@localhost", "Hello World");
        // END SNIPPET: e2

        MockEndpoint.assertIsSatisfied(context);
    }

    private void assertMailbox(MockEndpoint mock) {
        mock.reset();
        mock.expectedMessageCount(1);
        mock.expectedBodiesReceived("Hello World");
        mock.expectedHeaderReceived("cc", "james@localhost");
    }

    
    // *************************************
    // Config
    // *************************************

    @Configuration
    public class TestConfiguration {

        @Bean
        public RouteBuilder routeBuilder() {
            return new RouteBuilder() {
                @Override
                public void configure() {
                    from("pop3://claus@localhost?initialDelay=100&delay=100").to("mock:claus");

                    from("pop3://willem@localhost?initialDelay=100&delay=100").to("mock:willem");

                    from("pop3://hadrian@localhost?initialDelay=100&delay=100").to("mock:hadrian");

                    from("pop3://tracy@localhost?initialDelay=100&delay=100").to("mock:tracy");
                }
            };
        }
    }
    
   

}
