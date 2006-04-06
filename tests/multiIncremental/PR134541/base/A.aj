public aspect A {
	
	pointcut p() : execution(* *.*(..));
	
	before() : p() {
	}
	
}
