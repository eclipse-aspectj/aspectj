package hello;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class HelloWorld {

	public void println () {
		System.out.println("Hello World!");
	}

	private void testStackTrace () throws IOException {
		try {
			println();
		}
		catch (Exception ex) {
			printRelevantStackEntries(ex,getClass().getName());
		}
	}
	
	private static void printRelevantStackEntries (Exception ex, String className) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		ex.printStackTrace(ps);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		BufferedReader br = new BufferedReader(new InputStreamReader(bais));
		String entry;
		while ((entry = br.readLine()) != null) {
			if (entry.indexOf(className) != -1) {
				System.err.println(entry);
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		new HelloWorld().testStackTrace();
	}

}
