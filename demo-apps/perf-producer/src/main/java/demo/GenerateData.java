package demo;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GenerateData {

    @Value("${tempLargeFilePath}")
    String tempLargeFilePath;

    @Value("${targetLargeFilePath}")
    String targetLargeFilePath;

	@Value("${tempLargePath}")
	String tempLargePath;

	@Value("${targetLargePath}")
	String targetLargePath;

    public void generate(int size) throws IOException, URISyntaxException {
		
		File tempLarge = new File(tempLargePath);
		boolean dirCreated = tempLarge.mkdir();

		File targetLarge = new File(targetLargePath);
		boolean targetLargePathCreated = targetLarge.mkdir();
		
		byte[] mockData = IOUtils.toByteArray(this.getClass().getClassLoader().getResourceAsStream("data/root.xml"));;
		String xml = new String(mockData);
		String block = xml.substring(xml.indexOf("<root>")+"<root>".length(),xml.indexOf("</root>"));
		System.out.println(block.substring(0, 30));
		System.out.println(block.substring(block.length()-30, block.length()));
		long totalBytes = 0;
		//create a large file
		Files.write(Paths.get(tempLargeFilePath), "<root>".getBytes(), StandardOpenOption.CREATE);
		while (totalBytes < (1024*1024*size)){
			Files.write(Paths.get(tempLargeFilePath), block.getBytes(), StandardOpenOption.APPEND);
			totalBytes+=mockData.length;
	    }
        Files.write(Paths.get(tempLargeFilePath), "\n</root>".getBytes(), StandardOpenOption.APPEND);
        Files.move(Paths.get(tempLargeFilePath), Paths.get(targetLargeFilePath), StandardCopyOption.REPLACE_EXISTING);

	}

}