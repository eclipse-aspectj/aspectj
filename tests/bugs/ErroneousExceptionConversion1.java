import org.aspectj.lang.*;
import org.aspectj.testing.Tester;

aspect Watchcall {
	pointcut myConstructor(): execution(new(..));

	before(): myConstructor() {
		System.err.println("Entering Constructor");
	}

	after(): myConstructor() {
		System.err.println("Leaving Constructor");
	}
}

public class ErroneousExceptionConversion1 {
	public static void main(String[] args) {
		try {
			ErroneousExceptionConversion1 c = new ErroneousExceptionConversion1();
			Tester.checkFailed("shouldn't get here");
		} catch (NoAspectBoundException nab) {
			// expected
		}
		
	}
}
