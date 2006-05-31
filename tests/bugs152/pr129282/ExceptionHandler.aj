// with the exception handler, the advice isn't actually throwing the
// exception for the method - therefore, expect warnings when the methods
// don't throw the exception themselves.
public aspect ExceptionHandler {

	pointcut p() : handler(*);
	
	before() throws MyException : p() {
		throw new MyException();
	}
	
	
}

class C {
	
	public void method1() {
		try {
			new C().throwingMethod();
			new C().throwingMethod2();
		} catch (MyException e) {
			e.printStackTrace();
		}
	}
	
	// dont want 'declared exception not actually thrown'
	// warning for this method because it's throwing it
	public void throwingMethod() throws MyException {
		throw new MyException();
	}
	
	// do want 'declared exception not actually thrown'
	// warning because it doesn't throw it
	public void throwingMethod2() throws MyException {
	}
}

class MyException extends Exception {
}
