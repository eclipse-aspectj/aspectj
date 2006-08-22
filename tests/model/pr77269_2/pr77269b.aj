class Test {
	
	public void testMethod() {
		new Runnable() {
			public void run() {
			}
		};
		class C {
			public void m(){			
			}
		}
	}
	
}

aspect A {
	
	pointcut p() : execution(* m(..));
	
	before() : p() {
	}

}
