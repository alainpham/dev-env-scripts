package demo;

import java.io.ByteArrayInputStream;
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

import org.hornetq.api.core.DiscoveryGroupConfiguration;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.ClientProducer;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.api.core.client.ClientSessionFactory;
import org.hornetq.api.core.client.ClusterTopologyListener;
import org.hornetq.api.core.client.HornetQClient;
import org.hornetq.api.core.client.ServerLocator;
import org.hornetq.api.core.client.TopologyMember;
import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.HornetQJMSConstants;
import org.hornetq.api.jms.JMSFactoryType;
import org.hornetq.core.client.impl.ServerLocatorImpl;
import org.hornetq.core.client.impl.TopologyMemberImpl;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.remoting.impl.netty.TransportConstants;
import org.hornetq.jms.client.HornetQConnectionFactory;
import org.hornetq.jms.client.HornetQJMSConnectionFactory;

public class Application {

    public static void main(String[] args) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();

        // THIS WORKS*
        /* 

            alternative amq-broker-a-acceptor-0-amq-messaging-a.apps.cluster-33cc.33cc.example.opentlc.com
https://amq-broker-a-acceptor-0-appdev-amq-interconnect.apps.my-cluster.ocp4.openshift.es/
            cp tls/ocp/* /deployments/tls 
            export AMQHOST=amq-broker-a-acceptor-hornetq-0-amq-messaging-a.apps.cluster-33cc.33cc.example.opentlc.com
            export AMQPORT=443
       
            cp /home/workdrive/TAZONE/RUN/apps/messaging-tester/tls/* /deployments/tls 
            export AMQHOST=amq-broker-a-acceptor-0-appdev-amq-interconnect.apps.my-cluster.ocp4.openshift.es
            export AMQPORT=443

            cp tls/local/* /deployments/tls 
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
        // sl.createSessionFactory();
        // factory.setConnectionLoadBalancingPolicyClassName("org.hornetq.api.core.client.loadbalance.FirstElementConnectionLoadBalancingPolicy");
        // ServerLocator sl = HornetQClient.createServerLocator(false, config);
        // sl.setInitialConnectAttempts(5);
        // System.out.println(sl.getInitialConnectAttempts());
        // factory=new HornetQJMSConnectionFactory(sl);
        // ClientSession hqsess = sl.createSessionFactory(config).createSession("normaluser", "userpassword", false, true, true, true, 1000);
        // ClientSessionFactory hqcf = factory.getServerLocator().createSessionFactory(config);
        // System.out.println("ClientSessionFactory + " created );
        // ClientSession hqsess = hqcf.createSession("normaluser", "userpassword", false, true, true, true, 1000);
        // ClientProducer cp = hqsess.createProducer("app.queue");
        // ClientMessage mess = hqsess.createMessage(true);
        // mess.putStringProperty("test",  "value");
        // cp.send(mess);

        System.out.println("NB OF MEMBERS " + factory.getServerLocator().getTopology().getMembers().size());;
        System.out.println(Arrays.asList(factory.getServerLocator().getStaticTransportConfigurations()));
        System.out.println(factory.getServerLocator().isHA());
        System.out.println(factory.getServerLocator().getTopology().describe());
        System.out.println("Created factory..");
        Connection connection = factory.createConnection("normaluser","userpassword");
        System.out.println("Created...");

        connection.start();
        System.out.println("Created connection..");
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue q = session.createQueue("app.queue");
        MessageProducer producer = session.createProducer(q);
        System.out.println("connected");
        System.out.println("sending...");
        int i =0;
        TextMessage message = session.createTextMessage("{\"hello\":\"test "+ new Date() +" BALBALBALBAL\"}");
        producer.send(message);
        System.out.println("sent " + i + " messages");

        System.out.println(Arrays.asList(factory.getServerLocator().getStaticTransportConfigurations()));
        System.out.println(factory.getServerLocator().isHA());
        System.out.println(factory.getServerLocator().getTopology().describe());
        System.out.println("SENT OK!!!");
        Thread.sleep(100000);
    }
}

