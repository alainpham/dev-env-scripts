package demo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.jms.ConnectionFactory;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestSvc {

    @Autowired 
    private JmsTemplate jmsTemplate;


    @GetMapping("/send")
	public Object greeting() {
        Map<String,Object> obj = new HashMap<>();
        obj.put("hello", "world");
        obj.put("ping", "pong");
        jmsTemplate.convertAndSend(Application.queue, "{\"Id\":\"toto\",\"name\":\"alain\"}");
		return obj;
	}

    @Autowired
    DefaultJmsListenerContainerFactoryConfigurer jmsListenerContainerFactoryConfigurer;

    @GetMapping("/info")
    public Object info(){
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        ConnectionFactory cf = new JmsConnectionFactory();
        jmsListenerContainerFactoryConfigurer.configure(factory, cf);
        return "hello";
    }
}
