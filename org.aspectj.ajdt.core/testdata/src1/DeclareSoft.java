import java.io.*;
import org.aspectj.lang.*;

public class DeclareSoft {
	public static void main(String[] args) {
		try {
			new DeclareSoft().m();
		} catch (SoftException se) {
			return;
		}
	}
	
	void m() {
		InputStream s = new FileInputStream("foobar");
		s.close();
	}
}


aspect A {
	//declare soft: IOException: execution(void m());
	
	declare soft: IOException: foo();
	
	pointcut foo(): execution(void m());
}
