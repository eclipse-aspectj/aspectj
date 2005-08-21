public aspect HasMethodViaITD {
	
	declare parents : hasmethod(* foo()) implements I;
	
	// C gets foo via ITD
	public void C.foo() {}
	
	declare warning : execution(* I+.bar()) : "hasmethod matched on ITD ok";
}

interface I {}

class C {
	
	void bar() {}
	
}