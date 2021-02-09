package demo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.stereotype.Component;


@Component
public class LargeMessageConverter implements MessageConverter {

	//method to map ojbect to outgoing JMS message
	public Message toMessage(Object object, Session session) throws JMSException, MessageConversionException
	{
		//create JMS message
		BytesMessage message = session.createBytesMessage();

		//Use buffers for performance
		BufferedInputStream bufferedInput = new BufferedInputStream((InputStream)object);

		//Following Artemis documentation, set the InputStream with the property 'JMS_AMQ_InputStream'
		message.setObjectProperty("JMS_AMQ_InputStream", bufferedInput);

		//return JMS message
		return message;
	}


	public Object fromMessage(Message message) throws JMSException, MessageConversionException
	{
		
		//We use pipes to allow provide an InputStream to Camel's File component
		PipedInputStream pin = new PipedInputStream();
		PipedOutputStream out;


		try {
			out = new PipedOutputStream(pin);
		
			//use case: receive very large files and write to file.
			//we want to stream the bytes, not to load in memory the whole message.
			new Thread(
					  new Runnable() {
					      public void run() {
					    	  try {
								message.setObjectProperty("JMS_AMQ_SaveStream", out);
							} catch (JMSException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					      }
					},"OutStreamSaver").start();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	


		//return the InputStream which will be made available in the Body of the Exchange.
		//The File component in the Camel route will pick it up and write to file.
		return pin;
	}
}