public aspect DeclareAndInjar {
	
	pointcut p() : call(* *.*(..)) && !within(DeclareAndInjar);
	declare warning: p() : "warning";

	pointcut exec() : execution(* *.*(..)) && !within(DeclareAndInjar);
	declare error : exec() : "error";

}
