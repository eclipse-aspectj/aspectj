
public aspect AspectInDefaultPackage {

	public pointcut execM1() : execution(* pack.C.method1(..));
	public pointcut execM2() : execution(* pack.C.method2(..));
	
	before() : execM1() && this(pack.C) {		
	}
	
	before() : execM2() || execM1() {
	}
	
	before() : execution(* pack.C.method1()) {
	}

	
	
}
