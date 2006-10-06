package pkg;

public aspect AspectOnAspectPath {

	pointcut p() : call(* *.*(..)) && !within(AspectOnAspectPath);
	declare warning: p() : "blah";
	
	before() : p() {
	}
	
	pointcut exec() : execution(* *.*(..)) && !within(AspectOnAspectPath);
	declare warning : exec() : "blah2";
	before() : exec() {
		
	}
	
}
