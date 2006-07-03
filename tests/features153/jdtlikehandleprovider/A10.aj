package pkg;

public aspect A10 {

	pointcut p() : call(public void C.m2());
	
	before() : p() {
		
	}
	
}

class C {

	public void m1() {
		new C().m2();
	}
	
	public void m2() {
		
	}
}
