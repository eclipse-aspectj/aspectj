

/** Bugzilla Bug 34210  
   thisJoinPoint.getArgs() causes IncompatibleClassChangeError  */
public class ThisJoinPointAndVerifier {
	public void method() {
		System.out.println("Executed method");
	}
	public static void main(String args[]) {
		ThisJoinPointAndVerifier td = new ThisJoinPointAndVerifier();
		td.method();
	}
}

aspect Log1 {  
	pointcut logged_method() : 
		call(* ThisJoinPointAndVerifier.*(..));    
	after() : logged_method() {
		Object[] args = thisJoinPoint.getArgs(); 	
		System.out.println ("Log1a: leaving " + thisJoinPoint.getSignature());
	}    
	// comment this advice for scenario 2
	after() : logged_method() {
		//VM crash on scenario 1
		//Object[] args = thisJoinPoint.getArgs(); 
		System.out.println ("Log1b: leaving " + thisJoinPoint.getSignature());    
	}
}