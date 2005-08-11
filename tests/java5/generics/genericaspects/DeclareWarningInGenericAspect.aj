abstract aspect SuperAspect<T> {
	
	pointcut takesAT() : execution(* *(T));

	declare warning : takesAT() : "this method takes a T!";
}


public aspect DeclareWarningInGenericAspect extends SuperAspect<String> {
	
}

class C {
	
	// should be matched
	public void foo(String s) {}
	
	// should not be matched
	public void bar(Number n)  {}
	
	
}