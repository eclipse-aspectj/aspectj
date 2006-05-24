import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public aspect One {
	public static void main(String[] args) {
		
	}
	
	before(): call(* println(..)) {}
	
	//	 ... that does something ...
	public synchronized void b() {
		System.out.println("hello");
	}
	
	// ... that includes try/catch ...
	public synchronized void c() {
		try {
			File f = new File("fred");
			FileInputStream fis = new FileInputStream(f);
		} catch (IOException ioe) {
			System.out.println("bang");
		}
	}
	
	// ... with nested synchronized blocks ...
	public synchronized void e() {
		System.out.println("hello");
		synchronized (new String()) {
			System.out.println("other");
		}
	}

}

aspect OneX {
  pointcut p(): lock();
}
