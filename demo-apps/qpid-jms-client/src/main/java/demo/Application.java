package demo;

import java.time.Instant;

import javax.jms.DeliveryMode;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;


public class Application {

    public static String queueName="app.queue.a";

    public static void main(String[] args) throws InterruptedException, JMSException {
        
        // creating consumers
        JMSSingleton.createQueueConsumer(new Listener(), queueName);

        JMSSingleton.getSingletonConnection().setExceptionListener(new ExceptionListener() {
            @Override
            public void onException(JMSException exception) {
                System.out.println("ERROR ON CONNECTION EXCEPTION LISTENER " + exception.getClass().getName());
                System.out.println("Recreating consumer on " + queueName);
                while (true) {
                    try {
                        JMSSingleton.createQueueConsumer(new Listener(), queueName);
                        return;
                    } catch (JMSException e) {
                        System.out.println("Unable to recreate consumer at the moment retrying in a moment ...");
                        e.printStackTrace();
                    }

                    // waiting for before retrying
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // creatin producer
        MessageProducer producer = JMSSingleton.createQueueProducer(queueName);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        Integer i=0;
        while (true) {

            System.out.println("PRODUCER : Trying to send.. " + "{\"id\":"+i+"}" );
            try {
                producer = JMSSingleton.createQueueProducer(queueName);
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                TextMessage m = JMSSingleton.getSingletonSession().createTextMessage("{\"id\":"+i+"}");
                int begin = Instant.now().getNano();
                producer.send(m);
                System.out.println(Double.valueOf(Instant.now().getNano() - begin) / 1000000.00);
                System.out.println("PRODUCER : " + Thread.currentThread().getName() + " sending done " + m.getText());
                producer.close();
                i++;
            } catch (JMSException e) {
                System.err.println("Message Producer is in error state, recreating producer...");
                e.printStackTrace();
                try {
                    producer = JMSSingleton.createQueueProducer(queueName);
                } catch (JMSException e1) {
                    System.out.println("Unable to recreate producer at the moment");
                }
            }
            Thread.sleep(5000);

        }
    }

}