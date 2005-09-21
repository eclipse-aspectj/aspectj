public aspect PCLib {
	
	public pointcut anyMethodExecution() : execution(* *(..));
	
	public pointcut joinPointWithStringArg(String s) : args(s);
	
}