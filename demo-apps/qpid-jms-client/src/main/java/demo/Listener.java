package demo;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

public class Listener implements MessageListener{

    @Override
    public void onMessage(Message message) {
        try {
            System.out.println("CONSUMER : " + Thread.currentThread().getName() + " " + message.getBody(String.class).toString());
        } catch (JMSException e) {
            e.printStackTrace();
        }
        
    }
    
}
