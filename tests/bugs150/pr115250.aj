public class pr115250 {
	public static void main(String[] args) {
		test();
	}
	public static void test() {
		new C();
	}

	static class C {
		C() {
			System.err.println("C.new() running");
		}		
	}

	// properly get compiler error wrt return type of join point
	static aspect Normal {
		C around() : execution(C.new()) {
			return proceed();
		}
	}
	
	
	// no compiler error wrt return type of join point
	
	static abstract aspect SS<Target> {
		abstract protected pointcut creation();
		Target around() : creation() { // expect CE for execution(C.new());
			System.err.println("Advice running");
			return proceed(); 
		}
	}
	
	static aspect A extends SS<C> {
		protected pointcut creation() : execution(C.new());
	}
}
