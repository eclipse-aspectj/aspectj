// pr 44587
import org.aspectj.testing.Tester;
import org.aspectj.lang.NoAspectBoundException;
public class ErroneousExceptionConversion {
	
	public static void main(String[] args) {
		try {
			new ErroneousExceptionConversion();
			Tester.checkFailed("Wanted an exception in initializer error");
		} catch (NoAspectBoundException nabEx) {
			// good
			// check nabEx.getCause instanceof RuntimeException and has explanation "boom..."
			Throwable cause = nabEx.getCause();
			if (!(cause instanceof RuntimeException)) {
				Tester.checkFailed("Should have a RuntimeException as cause");
			}
		} catch(Throwable t) {
			Tester.checkFailed("Wanted an ExceptionInInitializerError but got " + t);
		}
	}
	
			
}

aspect A {
	
	int ErroneousExceptionConversion.someField = throwIt();
	
	public static int throwIt() {
		throw new RuntimeException("Exception during aspect initialization");
	}
	
	public A() {
		System.err.println("boom in 5...");
		throw new RuntimeException("boom");
	}
	
	// if I change this to execution the test passes...
	after() throwing :  initialization(ErroneousExceptionConversion.new(..)) {
		System.out.println("After throwing");
	}
	
}

