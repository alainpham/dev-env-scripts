package poc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileGrabber {

    @Value("${data.folder}")
    String fileFolder;


    public InputStream grab(String fileName) throws IOException{

        return FileUtils.openInputStream(new File(fileFolder+"/"+fileName));

    }
}
