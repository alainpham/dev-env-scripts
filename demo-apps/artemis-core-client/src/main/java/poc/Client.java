package poc;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class Client {

    // protected String brokerUrl;
    // protected String user;
    // protected String pwd;
    // protected String queueName;
    // protected String clientId;

    protected Connection connection;
    protected Session session;
    protected Destination destination;

    public Client()
            throws JMSException, NamingException {
        // this.brokerUrl = brokerUrl;
        // this.user = user;
        // this.pwd = pwd;
        // this.queueName = queueName;
        // this.clientId = clientId;
        
        Context context = new InitialContext();

        // Create a ConnectionFactory
        ConnectionFactory connectionFactory = (ConnectionFactory) context.lookup("standard");
        // Create a Connection
        connection =  connectionFactory.createConnection();
        connection.start();

        // Create a Session
        session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        
        // Create the destination (Topic or Queue)
        destination = (Destination) context.lookup("app.queue");
    }
}
