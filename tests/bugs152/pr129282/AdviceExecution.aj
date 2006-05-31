import java.io.FileNotFoundException;

import org.aspectj.lang.annotation.AdviceName;

public aspect AdviceExecution {
	
	// don't want the 'declared exception not actually
	// thrown' warning against this piece of advice
	@AdviceName("Test")
	before() throws FileNotFoundException : execution(* C.method1(..)) {	
	}
	
	before(AdviceName name) throws FileNotFoundException : adviceexecution() 
		&& @annotation(name)
		&& if(name.value().indexOf("Test") != -1) {
			throw new FileNotFoundException();
	}

}

class C {
	
	// don't want the 'declared exception not actually 
	// thrown' warning
	public void method1() throws FileNotFoundException {
	}
}
