public aspect MyAspect {
	
	declare warning : execution(* *(..)) : "a method";
	
}