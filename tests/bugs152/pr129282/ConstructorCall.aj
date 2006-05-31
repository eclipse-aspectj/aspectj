import java.io.FileNotFoundException;

public aspect ConstructorCall {

	pointcut p() : call(public C1.new());
	
	before() throws FileNotFoundException : p() { 
		throw new FileNotFoundException();
	}
	
}

class C1 {
	
	// shouldn't get the warning against the constructor
	public C1() throws FileNotFoundException {	
	}
	
	public void m1() throws FileNotFoundException {
		new C1();
	}
	
}
