package pkg;

public aspect A {

	pointcut p() : call(* pkg.*.*(..));
	
	before() : p() {
		    
	} 
}

