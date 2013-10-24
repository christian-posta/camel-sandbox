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

import org.apache.activemq.broker.jmx.BrokerViewMBean;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;


/**
 * @author <a href="http://www.christianposta.com/blog">Christian Posta</a>
 */
public class AddNetworkConnJmx {

    private static final String JMX_URL = "service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi";

    public static void main(String[] args) throws Exception{

        JMXServiceURL url = new JMXServiceURL(JMX_URL);
        JMXConnector connector = JMXConnectorFactory.connect(url);

        MBeanServerConnection serverConnection = connector.getMBeanServerConnection();

        ObjectName objectName = new ObjectName("org.apache.activemq:type=Broker,brokerName=amq-5.9");
        BrokerViewMBean proxy = JMX.newMBeanProxy(serverConnection, objectName, BrokerViewMBean.class, true);

        System.out.println("BrokerName: "  + proxy.getBrokerName());
        System.out.println("Adding NC: " + proxy.addNetworkConnector("static:(tcp://localhost:61617)"));

    }
}
