import java.io.FileNotFoundException;

aspect A {

	pointcut handlerPointcut() : handler(FileNotFoundException);
	
	before() : handlerPointcut() {	
	}
	
}

class C {
	
	public void method() {
		try {
			exceptionMethod();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			exceptionMethod();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	public void exceptionMethod() throws FileNotFoundException {
	}
}
