import java.io.FileNotFoundException;

aspect InnerMethodCall2 {
	
	pointcut p() : call(* C1.c1Method());
	
	before() throws FileNotFoundException : p() {
		throw new FileNotFoundException();
	}
	
}

class MainClass {
	
	public void amethod() {
		new C() {
			public void mymethod() throws FileNotFoundException {
				new C() {
					public void mymethod() throws FileNotFoundException {
						new C1().c1Method();
					}
				};
			}
		};
	}
	
}

class C1 {
	
	// don't want the 'declared exception not actually thrown'
	// exception because the advice is effectively throwing it
	public void c1Method() throws FileNotFoundException {
	}
	
}

abstract class C {
	public abstract void mymethod() throws FileNotFoundException;
}
