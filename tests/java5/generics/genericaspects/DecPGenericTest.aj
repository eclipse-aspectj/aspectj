
abstract aspect DecPGenericTest<X,Y> {
	
	declare parents : X implements Y;
	
}

aspect Sub extends DecPGenericTest<C,I> {
	
	declare warning : execution(* I+.foo()) : "success";
	
}

class C {
	
	void foo() {}
	
}

interface I {}

