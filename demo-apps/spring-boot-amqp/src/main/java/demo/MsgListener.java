package demo;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class MsgListener implements MessageListener{

    @Override
    public void onMessage(Message message) {
        TextMessage txt = (TextMessage)message;
        try {
            System.out.println(txt.getText());
            // txt.acknowledge();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
    
}
