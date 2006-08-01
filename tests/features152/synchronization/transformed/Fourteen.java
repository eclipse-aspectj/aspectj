import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.*;

/**
 * We had a bug where if Xjoinpoints:synchronized was ON and yet no pointcuts used lock()/unlock() *and*
 * a synchronized method was woven, then we got things wrong.  We removed the synchronized modifier but
 * never inserted the required monitorenter/exit block.
 */
public aspect Fourteen {
	
	public static void main(String[] args) throws Exception {
		Class c = Class.forName("Fourteen");
		Method m = c.getMethod("b",null);
		if (!Modifier.isSynchronized(m.getModifiers())) 
			throw new RuntimeException("Method b() should still be synchronized");
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

