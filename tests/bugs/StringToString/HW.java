
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

public class HW extends ArrayList {

  String message = "Hello World!";

	private void check (String args) {
	}

	public void println () {
		System.out.println(message);
	}

	public static void main(String[] args) {
		HW hw = new HW();
		hw.println();		
		for (int i = 0; i < args.length; i++) {
			String jp = args[i];
			if (!hw.contains(jp)) {
				throw new RuntimeException(jp + " missing"); 
			}
		}
	}

}
