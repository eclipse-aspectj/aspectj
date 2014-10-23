import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InvalidClassException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
//import java.util.Arrays;
//import java.util.List;

import org.aspectj.testing.Tester;

public class Util {

	public final static String DEFAULT_COMMAND = "-read";
	public final static String DEFAULT_NAME = "test.ser";

	public static void fail (String name) throws Exception {
		try {
			Object obj = read(name);
			Tester.checkFailed("java.io.InvalidClassException");
		}
		catch (InvalidClassException ex) {
			System.out.println("? Util.fail() ex=" + ex);
		}
	}

	public static Object read (String name) throws Exception {
		Object obj;
		File file = new File(name);
		file.deleteOnExit();
		ObjectInputStream in = null; 

		try {
			in = new ObjectInputStream(new FileInputStream(file));
			obj = in.readObject();
			System.out.println("? Util.read() obj=" + obj);
		}
		finally {
			in.close();
		}
		
		return obj;
	}

	public static void write (String name, Object obj) throws IOException {
		
		File file = new File(name);
//		File file = File.createTempFile(name,null);
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
		out.writeObject(obj);
		out.close();

		System.out.println("? Util.write() suid=" + ObjectStreamClass.lookup(obj.getClass()));
	}
	
	public static void main (String[] args) throws Exception {
		String command = (args.length > 0)? args[0] : DEFAULT_COMMAND;
		String name = (args.length > 1)? args[1] : DEFAULT_NAME;

		if (command.equals("-read")) {
			Object obj = read(name);
			new File(name).delete();
		}
		else if (command.equals("-fail")) {
			fail(name);
		}			
//		if (args.length > 0) {
//		}
//		else {
//			System.out.println("Usage: Util -fail | -read [name]");
//		}
	}
}
