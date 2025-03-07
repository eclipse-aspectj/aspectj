import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.aspectj.testing.Tester;

public aspect  IfFalse {
	
	private static boolean x = false;
	
	pointcut p1() : if(false);
	
	pointcut p2() : if(  false  );
	
	pointcut p3() : if(x);
	
	pointcut p4() : within(IfFalse) && if(false);
	
	@SuppressAjWarnings("adviceDidNotMatch")
	after() returning : p1() {
		// should never get here
		Tester.checkFailed("if(false) matched!");
	}

	@SuppressAjWarnings("adviceDidNotMatch")
	after() returning : p2() {
		// should never get here
		Tester.checkFailed("if(   false   ) matched!");
	}

	@SuppressAjWarnings("adviceDidNotMatch")
	after() returning : p3() {
		// should never get here
		Tester.checkFailed("if(x) matched!");
	}

	@SuppressAjWarnings("adviceDidNotMatch")
	after() returning : p4() {
		// should never get here
		Tester.checkFailed("if(false) matched!");
	}
	
	public static void main(String[] args) {}
}