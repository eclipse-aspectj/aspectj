public aspect PrecedenceAnnotations {
	
	declare precedence : (@Security *), *;

	declare precedence : *, (@Performance *);
	
	public static void main(String[] args) {
		A a = new A();
		a.foo();
	}
}

@interface Security {}
@interface Performance{}

class A {
	pointcut foo() : execution(* foo());
	void foo() {}
}

aspect S1 {
	before() : A.foo() {
		System.out.println("S1");
	}
}

@Security aspect S2 {
	
	before() : A.foo() {
		System.out.println("@Security S2");
	}
	
}

aspect P1 {
	after() returning : A.foo() {
		System.out.println("P1");
	}
}

@Performance aspect P2 {
	after() returning : A.foo() {
		System.out.println("@Performance P2");
	}
}