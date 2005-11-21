public class pr115250_2 {
	public static void main(String[] args) {
		new C().foo();
	}

	static class C {
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
		abstract protected pointcut exec();
		Target around() : exec() { // expect CE for execution(C.new());
			System.err.println("funky advice running");
			return proceed(); 
		}
	}
	
	static aspect A extends SS<C> {
		protected pointcut exec() : execution(* C.foo());
	}
}
