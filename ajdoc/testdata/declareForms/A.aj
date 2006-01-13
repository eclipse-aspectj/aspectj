package foo;

public aspect A {
	
	pointcut p() : execution(* C.amethod(..));
	
	declare warning : p() : "warning";
	
	before() : p() {
	}
	
}

class C {
	
	public void amethod() {
	}
	
}
