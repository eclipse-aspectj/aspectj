package demo;

import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

public class ConverterTest {
	
	/**
	 * For signature we need an UTF-8 environment - don't ask my why.
	 * 
	 * @since 29-Jun-06
	 * @see http://mindprod.com/jgloss/encoding.html
	 */
/*
		String encoding = System.getProperty("file.encoding");
		String outEncoding = new OutputStreamWriter(System.out).getEncoding();
		System.out.println("file.encoding=" + encoding + " # system property");
		System.out.println("System.out encoding = " + outEncoding);
	}
*/
	
	public static void run() throws Exception {
	/**
	 * Do we really get the right bytes here if the encoding is wrong?
	 * @throws UnsupportedEncodingException 
	 */
		//String name = "B\u00f6hm";	// this works
		String name = "Böhm";
		System.out.println("Hello, my name is Mr. " + name);
		byte[] bytes = Converter.utf8encode(name);
		assertEquals(66, bytes[0]);
		assertEquals(-61, bytes[1]);
		assertEquals(-74, bytes[2]);
		assertEquals(104, bytes[3]);
		assertEquals(109, bytes[4]);
	}

	public static void assertEquals(int i, int b) throws Exception {
          if (i!=b) {
throw new RuntimeException("different");
 }
        }

}
