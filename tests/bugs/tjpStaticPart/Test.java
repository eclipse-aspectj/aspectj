package tjpStaticPart;

import java.io.*;

public class Test {

	public static void main(String[] args) throws Exception{
		try {
			FileInputStream in = new FileInputStream("file-does-not-exist");
		} catch (FileNotFoundException e) {
		}
		
	}
}
