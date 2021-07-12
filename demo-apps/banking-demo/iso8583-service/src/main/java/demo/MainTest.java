package demo;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.MessageFactory;
import com.solab.iso8583.impl.SimpleTraceGenerator;
import com.solab.iso8583.parse.ConfigParser;

public class MainTest {

    public static void print(IsoMessage m) {
        System.out.printf("TYPE: %04x\n", m.getType());
        for (int i = 2; i < 128; i++) {
            if (m.hasField(i)) {
                System.out.printf("F %3d(%s): %s -> '%s'\n", i, m.getField(i).getType(), m.getObjectValue(i),
                        m.getField(i).toString());
            }
        }
    }

	public static void main(String[] args) throws Exception {
		MessageFactory mfact = ConfigParser
				.createFromClasspathConfig("iso8583/config.xml");
		mfact.setAssignDate(true);
		mfact.setTraceNumberGenerator(new SimpleTraceGenerator((int) (System
				.currentTimeMillis() % 100000)));
		LineNumberReader reader = new LineNumberReader(new InputStreamReader(
            MainTest.class.getClassLoader().getResourceAsStream(
						"iso8583/parse.txt")));
		String line = reader.readLine();
		while (line != null && line.length() > 0) {
			IsoMessage m = mfact.parseMessage(line.getBytes(), 12);
			print(m);
			line = reader.readLine();
		}
		reader.close();

		// Create a new message
		System.err.println("NEW MESSAGE");
		IsoMessage m = mfact.newMessage(0x200);

        boolean isBinary = true;
		m.setBinary(isBinary);
		m.setValue(4, new BigDecimal("501.25"), IsoType.AMOUNT, 0);
		m.setValue(12, new Date(), IsoType.TIME, 0);
		m.setValue(15, new Date(), IsoType.DATE4, 0);
		m.setValue(17, new Date(), IsoType.DATE_EXP, 0);
		m.setValue(37, 12345678, IsoType.NUMERIC, 12);
		m.setValue(41, "TEST-TERMINAL", IsoType.ALPHA, 16);
		FileOutputStream fout = new FileOutputStream("/tmp/iso.bin");
		m.write(fout, 2);
		fout.close();
		print(m);
		System.err.println("PARSE BINARY FROM FILE");
		byte[] buf = new byte[2];
		FileInputStream fin = new FileInputStream("/tmp/iso.bin");
		fin.read(buf);
		int len = ((buf[0] & 0xff) << 4) | (buf[1] & 0xff);
		buf = new byte[len];
		fin.read(buf);
		fin.close();
		mfact.setUseBinaryMessages(isBinary);
		m = mfact.parseMessage(buf, mfact.getIsoHeader(0x200).length());
		print(m);

        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println(objectMapper.writeValueAsString(m));
	}

}
