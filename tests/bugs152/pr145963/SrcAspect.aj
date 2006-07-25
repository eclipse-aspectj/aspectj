package pkg;

public aspect SrcAspect {
	
	pointcut p() : execution(* *.*(..)) && !within(pkg.*);
	
	before() : p() {
	}
	
}
