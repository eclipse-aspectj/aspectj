package pkg;

public aspect A {

	pointcut p() : execution(public void C.method2(..));
	
	before() : p() {
	}
	
}
