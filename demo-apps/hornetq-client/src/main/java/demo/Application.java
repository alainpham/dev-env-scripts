package demo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.client.HornetQClient;
import org.hornetq.api.core.client.ServerLocator;
import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.JMSFactoryType;
import org.hornetq.core.client.impl.ServerLocatorImpl;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.remoting.impl.netty.TransportConstants;
import org.hornetq.jms.client.HornetQConnectionFactory;

public class Application {

    public static void main(String[] args) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();

        // THIS WORKS*
        /* 
            export AMQHOST=amq-broker-a-acceptor-0-amq-messaging-a.apps.cluster-33cc.33cc.example.opentlc.com
            export AMQPORT=443
       
            export AMQHOST=amqbrokera0
            export AMQPORT=5446
        */
        params.put("host",  System.getenv("AMQHOST")); //export AMQHOST=amqbrokera0
        params.put("port", System.getenv("AMQPORT")); //export AMQPORT=5446
        params.put(TransportConstants.SSL_ENABLED_PROP_NAME, true);
        params.put(TransportConstants.TRUSTSTORE_PATH_PROP_NAME,"/deployments/tls/truststore.jks");
        params.put(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME,"password");
        // params.put(TransportConstants.TRUSTSTORE_PROVIDER_PROP_NAME,"JKS");

        params.put(TransportConstants.KEYSTORE_PATH_PROP_NAME,"/deployments/tls/keystore.jks");
        params.put(TransportConstants.KEYSTORE_PASSWORD_PROP_NAME,"password");
        // params.put(TransportConstants.KEYSTORE_PROVIDER_PROP_NAME,"JKS");

        //
        // params.put("host", "amq-broker-a-acceptor-0-amq-messaging-a.apps.cluster-33cc.33cc.example.opentlc.com");
        // params.put("port", "443");
        // params.put(TransportConstants.SSL_ENABLED_PROP_NAME, true);
        // params.put(TransportConstants.TRUSTSTORE_PATH_PROP_NAME,"/home/workdrive/TAZONE/RUN/apps/messaging-tester/tls/truststore.p12");
        // params.put(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME,"password");
        // // params.put(TransportConstants.TRUSTSTORE_PROVIDER_PROP_NAME,"JKS");

        // params.put(TransportConstants.KEYSTORE_PATH_PROP_NAME,"/home/workdrive/TAZONE/RUN/apps/messaging-tester/tls/keystore.p12");
        // params.put(TransportConstants.KEYSTORE_PASSWORD_PROP_NAME,"password");
        // params.put(TransportConstants.KEYSTORE_PROVIDER_PROP_NAME,"JKS");
        
        // params.put(TransportConstants.HTTP_ENABLED_PROP_NAME,true);
        
        TransportConfiguration config = new TransportConfiguration(NettyConnectorFactory.class.getName(), params);
        HornetQConnectionFactory factory = HornetQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, config);
        ServerLocator sl= HornetQClient.createServerLocator(false, config);
        // sl.createSessionFactory();
        // factory.setConnectionLoadBalancingPolicyClassName("org.hornetq.api.core.client.loadbalance.FirstElementConnectionLoadBalancingPolicy");
        
        System.out.println(Arrays.asList(factory.getServerLocator().getStaticTransportConfigurations()));
        System.out.println(factory.getServerLocator().isHA());
        System.out.println(factory.getServerLocator().getTopology().describe());
        System.out.println("Created factory..");
        Connection connection = factory.createConnection("normaluser","userpassword");
        connection.start();
        System.out.println("Created connection..");
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue q = session.createQueue("app.queue");
        MessageProducer producer = session.createProducer(q);
        System.out.println("connected");
        System.out.println("sending...");
        int i =0;
        TextMessage message = session.createTextMessage("{\"hello\":\"test "+ new Date() +" \"}");
        producer.send(message);
        System.out.println("sent " + i + " messages");

        System.out.println(Arrays.asList(factory.getServerLocator().getStaticTransportConfigurations()));
        System.out.println(factory.getServerLocator().isHA());
        System.out.println(factory.getServerLocator().getTopology().describe());
        Thread.sleep(100000);
    }
}

