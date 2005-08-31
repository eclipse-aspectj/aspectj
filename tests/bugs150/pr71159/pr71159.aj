class A {
	
	private void foo() {}
	
}

class B extends A {
	
	protected void foo() {}
	
}

class C extends B {}


class D extends C {
	
	public void foo() {}
	
}

aspect X {
	
	void bar() {
		D d = new D();
		d.foo();
	}
	
	declare warning : call(* B.foo()) : "should match";
	declare warning : call(* A.foo()) : "should not match";
	
}