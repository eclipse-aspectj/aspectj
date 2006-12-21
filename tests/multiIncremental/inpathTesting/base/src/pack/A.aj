package pack;

public aspect A {

	pointcut execEverything() : execution(* *.*(..));
	
	declare warning : execEverything() : "blah";
	
}
