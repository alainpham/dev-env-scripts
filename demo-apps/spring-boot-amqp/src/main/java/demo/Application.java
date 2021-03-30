package demo;


import javax.jms.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.SimpleMessageListenerContainer;

@EnableJms
@SpringBootApplication
@Configuration
public class Application {

    public static final String queue = "app.queue.a";

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


    // @Bean
    // public JmsListenerContainerFactory containerFactory() {
    //     DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
    //     factory.setSessionTransacted(false);
    //     factory.setSessionAcknowledgeMode(JmsSession.CLIENT_ACKNOWLEDGE);
    //     factory.setConcurrency("1");
    //     return factory;
    // }

    // @Bean
    // SimpleMessageListenerContainer container(ConnectionFactory connectionFactory){
    //     SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
    //     container.setConnectionFactory(connectionFactory);
    //     container.setDestinationName(queue);
    //     container.setMessageListener(new MsgListener());
    //     return container;
    // }
    @Bean
    DefaultMessageListenerContainer dcontainer(ConnectionFactory connectionFactory){
        DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setDestinationName(queue);
        container.setMessageListener(new MsgListener());
        return container;

    }
}