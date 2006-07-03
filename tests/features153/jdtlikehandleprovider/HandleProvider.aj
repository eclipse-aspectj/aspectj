aspect TwoNamedPointcuts {
	
	pointcut p1() : execution(* C.method1());
	
	pointcut p2() : execution(* C.method2());
	
	before() : p1() || p2() {
		System.out.println("before...");
	}
	
}

class C {
	
	public void method1() {
	}
	
	public void method2() {
	}
	
}
