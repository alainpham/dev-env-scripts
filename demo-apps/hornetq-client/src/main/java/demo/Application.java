package demo;

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
import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.JMSFactoryType;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.remoting.impl.netty.TransportConstants;
import org.hornetq.jms.client.HornetQConnectionFactory;

public class Application {

    public static void main(String[] args) throws JMSException {
        Map<String, Object> params = new HashMap<String, Object>();

        // THIS WORKS


        params.put("host", "amqbrokera0");
        params.put("port", 5446);
        params.put(TransportConstants.SSL_ENABLED_PROP_NAME, true);
        params.put(TransportConstants.TRUSTSTORE_PATH_PROP_NAME,"/home/workdrive/TAZONE/WORKSPACES/ws-dev-env-as-code/amqbroker/tls/truststore.jks");
        params.put(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME,"password");
        params.put(TransportConstants.TRUSTSTORE_PROVIDER_PROP_NAME,"JKS");

        params.put(TransportConstants.KEYSTORE_PATH_PROP_NAME,"/home/workdrive/TAZONE/WORKSPACES/ws-dev-env-as-code/amqbroker/tls/keystore.p12");
        params.put(TransportConstants.KEYSTORE_PASSWORD_PROP_NAME,"password");
        params.put(TransportConstants.KEYSTORE_PROVIDER_PROP_NAME,"JKS");

        //

        // params.put("host", "amq-broker-a-hornetq-0-svc-rte-amq-messaging-a.apps.cluster-fbdc.fbdc.example.opentlc.com");
        // params.put("port", "443");
        // params.put(TransportConstants.SSL_ENABLED_PROP_NAME, true);
        // params.put(TransportConstants.TRUSTSTORE_PATH_PROP_NAME,"/home/workdrive/TAZONE/RUN/apps/messaging-tester/tls/truststore.jks");
        // params.put(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME,"password");
        // params.put(TransportConstants.TRUSTSTORE_PROVIDER_PROP_NAME,"JKS");

        // params.put(TransportConstants.KEYSTORE_PATH_PROP_NAME,"/home/workdrive/TAZONE/RUN/apps/messaging-tester/tls/keystore.jks");
        // params.put(TransportConstants.KEYSTORE_PASSWORD_PROP_NAME,"password");
        // params.put(TransportConstants.KEYSTORE_PROVIDER_PROP_NAME,"JKS");
        
        // params.put(TransportConstants.HTTP_ENABLED_PROP_NAME,true);
        
        TransportConfiguration config = new TransportConfiguration(NettyConnectorFactory.class.getName(), params);
        HornetQConnectionFactory factory = HornetQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, config);
        System.out.println("Created factory..");
        Connection connection = factory.createConnection("HAHA","test");
        connection.start();
        System.out.println("Created connection..");
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue q = session.createQueue("app.queue");
        MessageProducer producer = session.createProducer(q);
        TextMessage message = session.createTextMessage("Ping: " + new Date());
        producer.send(message);
        System.out.println("connected");
        System.out.println("done");
    }
}

