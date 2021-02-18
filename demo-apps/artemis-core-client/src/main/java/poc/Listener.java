package poc;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class Listener implements MessageListener {

    public void onMessage(Message message) {
        if (message instanceof BytesMessage) {
            BytesMessage byteMessage = (BytesMessage) message;

            try {
                System.out.println(
                        Thread.currentThread().getName() + " Receiving message : " + byteMessage.getJMSMessageID());
                File targetFile = new File(byteMessage.getJMSMessageID());
                FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
                BufferedOutputStream bufferedOutput = new BufferedOutputStream(fileOutputStream);
                byteMessage.setObjectProperty("JMS_AMQ_SaveStream", bufferedOutput);
                message.acknowledge();
                System.out.println("finished");

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        } else{
            TextMessage textMessage = (TextMessage) message;
            try {
                System.out.println(textMessage.getText());
                message.acknowledge();
            } catch (JMSException e) {
                e.printStackTrace();
            } 
        }

    }

}
