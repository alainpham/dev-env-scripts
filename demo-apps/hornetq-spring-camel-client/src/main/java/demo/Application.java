package demo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.env.Environment;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.camel.component.hystrix.metrics.servlet.HystrixEventStreamServlet;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.jms.JmsConfiguration;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.JMSFactoryType;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.remoting.impl.netty.TransportConstants;
import org.hornetq.jms.client.HornetQConnectionFactory;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@SpringBootApplication
@EnableWebSocket
// @ImportResource({"classpath:spring/camel-context.xml"})
public class Application implements WebSocketConfigurer{

    // must have a main method spring-boot can run
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    ServletRegistrationBean hystrixServletRegistrationBean() {
        ServletRegistrationBean mapping = new ServletRegistrationBean();
        mapping.setServlet(new HystrixEventStreamServlet());
        mapping.addUrlMappings("/hystrix.stream");
        mapping.setName("HystrixEventStreamServlet");

        return mapping;
    }

    @Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		
		// Expose endpoint and add Handler. Wildcard allowed origins to support COORS
		// registry.addHandler(new WebsocketTextHandler(), "/websocket").setAllowedOrigins("*");
		registry.addHandler(new WebSocketTextHandler(), "/websocket");
    }
    

    @Autowired
    private Environment env;
    private final static String CNX_FACTORY_NAME = "default";

    @Bean
    CachingConnectionFactory cachingConnectionFactory() throws JMSException, NamingException {
        
                /* 
            cp tls/ocp/* /deployments/tls 
            export AMQHOST=amq-broker-a-acceptor-hornetq-0-amq-messaging-a.apps.cluster-33cc.33cc.example.opentlc.com
            export AMQPORT=443
       
            cp tls/local/* /deployments/tls 
            export AMQHOST=amqbrokera0
            export AMQPORT=5446
        */
        Map<String, Object> params = new HashMap<String, Object>();

        params.put("host",  System.getenv("AMQHOST")); //export AMQHOST=amqbrokera0
        params.put("port", System.getenv("AMQPORT")); //export AMQPORT=5446
        params.put(TransportConstants.SSL_ENABLED_PROP_NAME, true);
        params.put(TransportConstants.TRUSTSTORE_PATH_PROP_NAME,"/deployments/tls/truststore.jks");
        params.put(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME,"password");
        // params.put(TransportConstants.TRUSTSTORE_PROVIDER_PROP_NAME,"JKS");

        params.put(TransportConstants.KEYSTORE_PATH_PROP_NAME,"/deployments/tls/keystore.jks");
        params.put(TransportConstants.KEYSTORE_PASSWORD_PROP_NAME,"password");

        TransportConfiguration config = new TransportConfiguration(NettyConnectorFactory.class.getName(), params);
        ConnectionFactory factory = (ConnectionFactory) HornetQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, config);
        
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(factory);
        return cachingConnectionFactory;
    }

    @Bean
    public JmsComponent jms(CachingConnectionFactory cachingConnectionFactory) {

        JmsConfiguration jmsConfiguration =new JmsConfiguration(cachingConnectionFactory);
        // jmsConfiguration.setCacheLevelName(env.getProperty("jms.cache.level"));
        JmsComponent jmsComponent = new JmsComponent(jmsConfiguration);

        return jmsComponent;
    }

}