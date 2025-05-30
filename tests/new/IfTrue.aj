import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.aspectj.testing.Tester;

public aspect  IfTrue {
	
	private static boolean x = true;
	
	pointcut p1() : !if(true);
	
	pointcut p2() : !if(  true  );
	
	pointcut p3() : !if(x) && execution(* *(..));
	
	pointcut p4() : within(IfTrue) && !if(true);
	
	@SuppressAjWarnings("adviceDidNotMatch")
	after() returning : p1() {
		// should never get here
		Tester.checkFailed("!if(true) matched!");
	}

	@SuppressAjWarnings("adviceDidNotMatch")
	after() returning : p2() {
		// should never get here
		Tester.checkFailed("!if(   true   ) matched!");
	}

	after() returning : p3() {
		// should never get here
		Tester.checkFailed("!if(x) matched!");
	}

	@SuppressAjWarnings("adviceDidNotMatch")
	after() returning : p4() {
		// should never get here
		Tester.checkFailed("!if(true) matched!");
	}
	
	public static void main(String[] args) {}
}