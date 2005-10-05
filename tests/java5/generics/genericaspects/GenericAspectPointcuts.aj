abstract aspect GenericAspectPointcuts<S,T,E> {
	
	/** 
	  test declare warning with :
		    - KindedPointcut
		       all parts of Signature pattern
		    - HandlerPointcut
		    - ReferencePointcut
		    - WithincodePointcut
		    - WithinPointcut
		    - And
		    - Or
		    - Not
	*/
	
	pointcut kindedWithReturning() : execution(T *(..));
	pointcut kindedWithDeclaring() : call(* S.*(..));
	pointcut kindedWithParams() : execution(* *(T));
	pointcut kindedWithThrows() : execution(* *(..) throws E);
	
	pointcut p1() : execution(* T.*(..));
	pointcut p2() : execution(S *(..));
	pointcut p3() : set(S a);
	pointcut p4() : get(* T.*);
	pointcut p5() : within(S || T);
	
	pointcut handlerPC() : handler(E);
	
	declare warning : kindedWithReturning() : "kinded-returning-ok";
	declare warning : kindedWithDeclaring() : "kinded-declaring-ok";
	declare warning : kindedWithParams() : "kinded-params-ok";
	declare warning : kindedWithThrows() : "kinded-throws-ok";
	
	declare warning : p1() && p2() : "and-ok";
	declare warning : p3() || p4() : "or-ok";
	declare warning : staticinitialization(*) && !p5() : "not-ok";
	
	declare warning : within(T) && staticinitialization(*) : "within-ok";
	declare warning : withincode(S T.*(..)) : "withincode-ok";
}

aspect Sub extends GenericAspectPointcuts<A,B,Z> {
	
	declare warning : handlerPC() : "handler-ok";  // also tests ref to super pointcut
}



class A {
	void bar(B b) {
		try {
			t();
		} catch( Z z) { System.out.println("z");}
	}
	
	void t() throws Z {}
}

class B {
    A a = new A();
	
	B newB() { return new B(); }
	
	A newA() { return a; }
	
	void foo() {
		a.bar(this);
	}
	
}

class Z extends Exception {}