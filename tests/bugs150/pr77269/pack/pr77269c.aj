package pack;

class Test {
	
	public void testMethod() {
		new Runnable() {
			public void run() {
				someMethod();
			}
		};
	}
	
	public void someMethod() {		
	}
}

aspect A {
	declare warning : call(void someMethod(..)) : "blah blah blah";
}
