import java.io.FileNotFoundException;

public aspect MethodCall {

	pointcut p() : call(public * C1.m2());
	
	before() throws FileNotFoundException : p() { 
		throw new FileNotFoundException();
	}
	
	pointcut p2() : call(public * C1.m4());
	
	before() : p2() {
	}
	
}

class C1 {
	
	public void m1() throws FileNotFoundException {
		new C1().m2();
	}
	
	// don't want the 'declared exception not actually
	// thrown' warning because the advice is affecting
	// this method
	public void m2() throws FileNotFoundException {		
	}
	
	public void m3() throws FileNotFoundException {
		new C1().m4();
	}
	
	// do want the 'declared exception not actually
	// thrown' warning
	public void m4() throws FileNotFoundException {
	}
}
