public aspect A {

	pointcut p() : execution(public void C.method1(..));
	
	before() : p() {
	}
	
}
