package demo;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.qpid.jms.JmsConnection;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.JmsSession;
public class JMSSingleton {
    private static final String URL = 
        System.getenv().getOrDefault
            ("BROKER_URL", 
            // "failover:(amqps://interconnect-cluster-a-5671-appdev-amq-interconnect.apps.my-cluster.ocp4.openshift.es:443)?failover.nested.transport.trustAll=true");
            "failover:(amqps://amq-broker-a-acceptor-0-appdev-amq-interconnect.apps.my-cluster.ocp4.openshift.es:443,amqps://amq-broker-a-acceptor-1-appdev-amq-interconnect.apps.my-cluster.ocp4.openshift.es:443)?failover.nested.transport.trustAll=true&failover.amqpOpenServerListAction=REPLACE");
            // "amqps://interconnect-cluster-a-5671-appdev-amq-interconnect.apps.my-cluster.ocp4.openshift.es:443?transport.trustAll=true");
    private static Connection singletonConnection=null;
    private static Session singletonSession=null;
    private static JmsConnectionFactory connectionFactory=null;

    public static synchronized Connection getSingletonConnection() throws JMSException
    {           
        connectionFactory = new JmsConnectionFactory(URL);
        if (singletonConnection == null){
            singletonConnection = connectionFactory.createConnection();
            singletonConnection.start();
            System.out.println("Connection started ...");
            connectionInfo();
        }
        
        return singletonConnection;
    }

    public static void createQueueConsumer(MessageListener listener,String queueName) throws JMSException{
        getSingletonSession().createConsumer(getSingletonSession().createQueue(queueName)).setMessageListener(listener);
    }

    public static MessageProducer createQueueProducer(String queueName) throws JMSException{
        return getSingletonSession().createProducer(getSingletonSession().createQueue(queueName));
    }

    public static synchronized Session getSingletonSession() throws JMSException
    {           
        JmsSession qpidSingletonSession = (JmsSession) singletonSession;
        // if (qpidSingletonSession == null) {
        //     System.out.println("SESSION REQUEST : Creating session for first time");
        //     singletonSession = JMSSingleton.getSingletonConnection().createSession();
        // }
        if (qpidSingletonSession == null || qpidSingletonSession.isClosed()){
            if (qpidSingletonSession==null) {
                System.out.println("SESSION REQUEST : Creating session for first time");
            }else{
                System.out.println("SESSION REQUEST : Session isClosed=" + qpidSingletonSession.isClosed() + " Recreating session from connection. " );
            }
            singletonSession = JMSSingleton.getSingletonConnection().createSession();
        }
        return singletonSession;
    }

    public static void connectionInfo(){
        if (singletonConnection!=null){
            try {
                System.out.println("Connection Factory isCloseLinksThatFailOnReconnect : " + connectionFactory.isCloseLinksThatFailOnReconnect());
                System.out.println("Connection ID : " + ((JmsConnection)singletonConnection).getId());
                System.out.println("Connection current uri : " + ((JmsConnection)singletonConnection).getConnectedURI());
                System.out.println("Connection is connected : " + ((JmsConnection)singletonConnection).isConnected());
                System.out.println("Connection JMS version : " + ((JmsConnection)singletonConnection).getMetaData().getJMSVersion());
                System.out.println("Connection JMS provider/version : " + ((JmsConnection)singletonConnection).getMetaData().getJMSProviderName() + "/"+((JmsConnection)singletonConnection).getMetaData().getProviderVersion());
            } catch (JMSException e) {
                e.printStackTrace();
            }

        }else{
            System.out.println("Connection is null");
        }
    }

}
