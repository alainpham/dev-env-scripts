package demo;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UiConfigRestController {
    
    private Map<String,String> uiConfig;

    public UiConfigRestController(Environment environment) {
        uiConfig = new TreeMap<String,String>();
        uiConfig.put("theme", environment.getProperty("theme"));
        uiConfig.put("jms.uri", environment.getProperty("jms.connectionFactory.default"));
        uiConfig.put("queue.defaultapp", environment.getProperty("queue.defaultapp"));

    }
    
    @GetMapping(value = "/uiconfig", produces = "application/json")
    Map<String,String> getUiConfig(){
        return uiConfig;
    }


}
