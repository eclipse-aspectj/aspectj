public aspect A1 {
	
	pointcut p() : execution(public void C.method2(..));
	
	before() : p() {
	}
}
