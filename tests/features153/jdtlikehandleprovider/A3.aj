package pkg;

aspect A3 {
	
	before() : execution(* *.*(..)) {
	}
	
}

class C {
	
	public void method1() {
	}
	
}
