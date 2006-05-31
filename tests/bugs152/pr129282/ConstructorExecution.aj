import java.io.FileNotFoundException;

public aspect ConstructorExecution {

	pointcut p1() : execution(public C1.new());
	
	before() throws FileNotFoundException : p1() {
		throw new FileNotFoundException();
	}
	
}

class C1 {
	
	// shouldn't get the warning on this constructor
	public C1() throws FileNotFoundException {	
	}
	
}
