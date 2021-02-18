package poc;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.naming.NamingException;

public class Consumer extends Client {

    public Consumer() throws JMSException, NamingException {
        super();
    }

    public void start() throws JMSException {

        MessageConsumer consumer = session.createConsumer(destination);
        System.out.println("start listening : " + destination.toString());
        consumer.setMessageListener(new Listener());
    }
    
}
