package demo;


import java.util.Arrays;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistry;

@EnableJms
@SpringBootApplication
@Configuration
public class Application {

    public static final String queue = "app.queue.a";

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    ApplicationContext applicationContext;


    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setReceiveTimeout(1000l);
        return factory;
    }

    // @Bean
    // SimpleMessageListenerContainer container(ConnectionFactory connectionFactory){
    //     SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
    //     container.setConnectionFactory(connectionFactory);
    //     container.setDestinationName(queue);
    //     container.setMessageListener(new MsgListener());
    //     return container;
    // }
    // @Bean
    // DefaultMessageListenerContainer dcontainer(ConnectionFactory connectionFactory){
    //     DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
    //     container.setConnectionFactory(connectionFactory);
    //     container.setDestinationName(queue);
    //     container.setMessageListener(new MsgListener());
        
    //     return container;

    // }


    @JmsListener(destination = queue)
    public void receiveMessage(Message message){
        TextMessage txt = (TextMessage)message;
        try {
            System.out.println(txt.getText());
        } catch (JMSException e) {
            e.printStackTrace();
        }
        // System.out.println(Arrays.asList(applicationContext.getBeanDefinitionNames()));
    }

}