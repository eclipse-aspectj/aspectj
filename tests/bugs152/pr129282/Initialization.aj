import java.io.FileNotFoundException;

public aspect Initialization {

	pointcut preInit() : preinitialization(C.new(String));
	
	before() throws FileNotFoundException : preInit() {
		throw new FileNotFoundException();
	}
	
	pointcut init() : initialization(C.new());
	
	before() throws FileNotFoundException : init() {
		throw new FileNotFoundException();
	}
}

class C {
	
	// shouldn't get a warning against this constructor  
	// since the throwing is handled by the advice
	public C() throws FileNotFoundException {
	}
	
	// shouldn't get a warning against this constructor
	// since the throwing is handled by the advice
	public C(String s) throws FileNotFoundException {
	}
	
}
