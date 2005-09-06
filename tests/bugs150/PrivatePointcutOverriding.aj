abstract aspect SuperAspect {
	
	private pointcut matchedJP() : execution(* foo(..));
	
	declare warning : matchedJP() : "matched join point from super advice";
	
}

public aspect PrivatePointcutOverriding extends SuperAspect {
	
	private pointcut matchedJP() : execution(* bar(..));
	
	declare warning : matchedJP() : "matched join point from sub advice";
	
}

class C {
	
	void foo() {}
	
	void bar() {}
	
}