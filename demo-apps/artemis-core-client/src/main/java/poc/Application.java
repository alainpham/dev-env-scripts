package poc;

import java.io.IOException;

import javax.jms.JMSException;
import javax.naming.NamingException;


public class Application {

    public static void main(String[] args) throws JMSException, NamingException, IOException, InterruptedException {
        System.out.println("param " + args[0]);

        if (args[0].equals("send")) {

             Producer producer = new Producer("/home/workdrive/TAZONE/RECORDINGS/tdf.mov");
            // Producer producer = new Producer("test.txt");
            producer.start();
        }else{
            Consumer consumer = new Consumer();
            consumer.start();
            System.out.println(Thread.currentThread().getName() + " waiting");
            Thread.currentThread().join();
        }
    }

}