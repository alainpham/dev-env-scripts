package demo;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class Consumer {
    
    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

    @Incoming("in-events")
    public void consume(String message) {              
        logger.info(message);
    }
}
