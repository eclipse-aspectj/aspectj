package pkg;

aspect A5 {
	
	pointcut p() : execution(* *.*(..));
	
	before() : p() {
	}
	
	before() : p() {
	}
}
