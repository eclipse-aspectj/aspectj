public aspect AJDKExamples {
	
	declare warning : call(* whoAreYou())
		: "call(* whoAreYou())";
	
	declare warning : call(* A.whoAreYou())
        : "call(* A.whoAreYou())";

	declare warning : call(A whoAreYou())
        : "call(A whoAreYou())";

	declare warning : call(A B.whoAreYou())
        : "call(A B.whoAreYou())";

	declare warning : call(A+ B.whoAreYou())
        : "call(A+ B.whoAreYou())";

	declare warning : call(B A.whoAreYou())
        : "call(B A.whoAreYou())";

	declare warning : call(B whoAreYou())
        : "call(B whoAreYou())";

	declare warning : call(B B.whoAreYou())
        : "call(B B.whoAreYou())";
	
}

class A {
	  public A whoAreYou() { return this; }
}
	
class B extends A {
	  // override A.whoAreYou *and* narrow the return type.
	  public B whoAreYou() { return this; }
}

class C {
	
	public C() {
		A a = new A();
		B b = new B();
		a.whoAreYou();
		b.whoAreYou();
	}
	
}