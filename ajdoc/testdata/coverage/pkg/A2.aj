package pkg;

public aspect A2 {
		
	pointcut p() : execution(* C2.amethod(..));
	pointcut p2() : execution(* C2.amethod(..));
	
	before() : p() {
	}
	
	before() : p2() {
	}
	
}

class C2 {
	
	public void amethod() {
	}
}
