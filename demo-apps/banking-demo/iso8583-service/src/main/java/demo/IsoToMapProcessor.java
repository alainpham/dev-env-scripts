package demo;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.MessageFactory;
import com.solab.iso8583.parse.ConfigParser;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

@Component
public class IsoToMapProcessor implements Processor{
 

    private MessageFactory mf;

    public IsoToMapProcessor() throws IOException {
        mf = ConfigParser.createFromClasspathConfig("iso8583/config.xml");
    }

    @Override
    public void process(Exchange exchange) throws Exception {

        String body = exchange.getIn().getBody(String.class);
        IsoMessage m = mf.parseMessage(body.getBytes(), 12);
        exchange.getIn().setBody(isoToMap(m));

    }
    
    Map<String,Object> isoToMap(IsoMessage m){
        Map<String,Object> map = new TreeMap<String,Object>();
        
        map.put("type", m.getType());

        for (Integer i = 2; i < 128; i++) {
            if (m.hasField(i)) {
                map.put(i.toString(),m.getObjectValue(i));
            }
        }        
        return map;
    }
}
