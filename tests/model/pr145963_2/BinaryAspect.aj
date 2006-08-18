package pkg;

public aspect BinaryAspect {

	declare warning : (get(* System.out) || get(* System.err)) : "There should be no printlns";

	pointcut p() : execution(* *.*(..));
	
	before() : p() {
	}
		
}
