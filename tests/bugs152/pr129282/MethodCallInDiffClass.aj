import java.io.FileNotFoundException;

public aspect MethodCallInDiffClass {

	pointcut p() : call(public * B1.m2());
	
	before() throws FileNotFoundException : p() { 
		throw new FileNotFoundException();
	}
	
}

class B {
	
	public void m1() throws FileNotFoundException {
		new B1().m2();
	}
	
}

class B1 {
	
	// don't want the 'declared exception not acutally
	// thrown' warning since the advice is throwing it
	public void m2() throws FileNotFoundException {	
	}
	
}
