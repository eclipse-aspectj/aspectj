import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Program {
	public static void main(String[] args) {
	  new Program().b();	
	  new Program().c();	
	  new Program().d();	
	}
	
	//	 ... that does something ...
	public synchronized void b() {
		System.out.println("hello from b()");
	}
	
	// ... that includes try/catch ...
	public synchronized void c() {
		try {
			File f = new File("fred");
			FileInputStream fis = new FileInputStream(f);
		} catch (IOException ioe) {
			System.out.println("bang in c()");
		}
	}
	
	// ... with nested synchronized blocks ...
	public synchronized void d() {
		System.out.println("hello from d()");
		synchronized (new String()) {
			System.out.println("hello from block in d()");
		}
	}

}
