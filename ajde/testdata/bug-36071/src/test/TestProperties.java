/*
 * Created on 30-Jul-03
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author websterm
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class TestProperties {

	public void load (String name) throws IOException {
		InputStream in = getClass().getResourceAsStream(name);
//		System.out.println("? load() in=" + in);
		Properties props = new Properties();
		props.load(in);
		in.close();		
		props.list(System.out);	
	}
}
