import org.aspectj.testing.Tester;

public aspect  IfFalse {
	
	private static boolean x = false;
	
	pointcut p1() : if(false);
	
	pointcut p2() : if(  false  );
	
	pointcut p3() : if(x);
	
	pointcut p4() : within(IfFalse) && if(false);
	
	
	after() returning : p1() {
		// should never get here
		Tester.checkFailed("if(false) matched!");
	}

	after() returning : p2() {
		// should never get here
		Tester.checkFailed("if(   false   ) matched!");
	}

	after() returning : p3() {
		// should never get here
		Tester.checkFailed("if(x) matched!");
	}

	after() returning : p4() {
		// should never get here
		Tester.checkFailed("if(false) matched!");
	}
	
	public static void main(String[] args) {}
}