package demo;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * @author oliver
 * @since 18.11.2005
 */
public class Converter {
	
	/**
	 * You may get problems using String.getBytes("UTF-8") on a non UTF-8
	 * environment. Here is an alternative for it from
	 * http://mindprod.com/jgloss/encoding.html.
	 * 
	 * @param text
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] utf8encode(String text) throws UnsupportedEncodingException {
		//return text.getBytes("UTF-8");
		Charset cs = Charset.forName("UTF8");
		CharBuffer ss = CharBuffer.wrap(text);
		ByteBuffer bb = cs.encode(ss);
		int limit = bb.limit();  // how many chars in buffer
		byte[] b = new byte[ limit ];
		bb.get( b, 0 /* offset */, limit );
		return b;
	}
	
}
