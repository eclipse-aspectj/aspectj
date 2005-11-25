package pack;

public aspect A {

	pointcut p1() : execution(* *.*(..));
	
	before() : p1() {}
	
}
