import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

public class LTWHelloWorld extends ArrayList {

	private String message = "Hello World!";

	public void println () {
		System.out.println(message);
	}

	public static void main(String[] args) {
		LTWHelloWorld hw = new LTWHelloWorld();
		hw.println();		
		for (int i = 0; i < args.length; i++) {
			String jp = args[i];
			if (!hw.contains(jp)) {
				throw new RuntimeException(jp + " missing"); 
			}
		}
	}

}
