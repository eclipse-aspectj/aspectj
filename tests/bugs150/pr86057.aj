abstract aspect Base {
	
	public final pointcut foo() : execution(* *(..));
	
}

aspect Sub extends Base {
	
	public pointcut foo() : execution(* *(..)) && args(String);
	
}