
import org.aspectj.testing.Tester;

/** Bugzilla Bug 34925  
   compiler crash on yesterday's rc1 build 
 */
import java.io.*;

public aspect ConvertToUnchecked {
	
	public static void main(String[] args) {
		try {
			Foo foo = new Foo("hello");
			Tester.check(false, "shouldn't get here");
		} catch (PersistenceException pe) {
		}
	}
	
    // convert IOExceptions in Foo to PersistenceException
    pointcut module() : within(Foo);
  
	after() throwing (IOException e) : module() {
	   throw new PersistenceException(e);
	}
    declare soft: (IOException): module();
}

class PersistenceException extends RuntimeException 
{
	Throwable cause;
  public PersistenceException(Throwable cause) {
    this.cause = cause;
  }
}


class Root {
	Root(String s) /*throws IOException*/ {
	}
}

class Foo extends Root {
	Foo(String s) {
		super(s);
	}
	
	static {
		if (false) {
			getFile();
			throw new IOException("bar");
		}
		
	}
	
	{
		if (false) throw new IOException("bar");
	}
	
	File f = getFile();
	
	static File getFile() throws IOException {
		throw new IOException("bad");
	}
	
	
	public void m() {
		throw new IOException("hi");
	}
}