package poc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.StreamMessage;
import javax.naming.NamingException;

import org.apache.commons.io.FileUtils;

public class Producer extends Client {



    private String inputFileName;


    public Producer(String inputFileName) throws JMSException, NamingException {
        super();
        this.inputFileName = inputFileName;
    }

    public void start() throws IOException, JMSException {
        InputStream is = FileUtils.openInputStream(new File(inputFileName));

        BufferedInputStream bufferedInput = new BufferedInputStream(is);

        MessageProducer producer = session.createProducer(destination);

        BytesMessage message = session.createBytesMessage();

        message.setObjectProperty("JMS_AMQ_InputStream", bufferedInput);
        producer.send(message);
    }
    

}
