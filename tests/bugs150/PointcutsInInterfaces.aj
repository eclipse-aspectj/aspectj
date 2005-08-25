public interface PointcutsInInterfaces {
	
	public pointcut foo() : execution(* *(..));
	
}

class C {
	void foo() {}
}

aspect A {
	
	declare warning : PointcutsInInterfaces.foo() : "aha!";
	
}