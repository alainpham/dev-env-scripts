package demo;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import org.apache.camel.EndpointInject;
import org.apache.camel.Header;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;

@Component
public class DataGenerator {

    private Random random = new Random();
    
    @EndpointInject(uri = "direct:send")
    ProducerTemplate producer;

    public byte[] getData(Integer size) {
        int leftLimit = 97; // letter 'a'
    int rightLimit = 122; // letter 'z'

    String generatedString = random.ints(leftLimit, rightLimit + 1)
      .limit(size)
      .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
      .toString();
        return generatedString.getBytes(StandardCharsets.US_ASCII);
    }

    public void spamData(@Header("records") Integer records,@Header("size") Integer size) {
        for (int i = 0; i < records; i++) {
            producer.sendBody(getData(size));
        }
    }
}