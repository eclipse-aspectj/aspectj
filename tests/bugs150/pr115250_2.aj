public class pr115250_2 {
	public static void main(String[] args) {
		test();
	}
	public static void test() {
		new C().foo();
	}

	static class C {
		C() {
			System.err.println("C.new() running");
		}		

                C foo() { return null; }
	}

	// properly get compiler error wrt return type of join point
	static aspect Normal {
		C around() : execution(* C.foo()) {
			return proceed();
		}
	}
	
	
	// no compiler error wrt return type of join point
	
	static abstract aspect SS<Target> {
		abstract protected pointcut creation();
		Target around() : creation() { // expect CE for execution(C.new());
			System.err.println("funky advice running");
			return proceed(); 
		}
	}
	
	static aspect A extends SS<C> {
		protected pointcut creation() : execution(* C.foo());
	}
}
