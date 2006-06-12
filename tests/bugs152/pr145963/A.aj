package pkg;

public aspect A {
	
	pointcut p() : execution(* *.*(..)) && !within(pkg.*);
	
	before() : p() {
	}
	
}
