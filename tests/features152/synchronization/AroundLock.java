// around advice and lock

public aspect AroundLock {

	String s = "foo";
//	void around(Object f): lock() && args(f) {
//		System.err.println("around(Object) lock: advice running at "+thisJoinPoint.getSourceLocation());
//		proceed(f);
//	}
	
	void around(Object f): lock() && args(f){
		System.err.println("around() lock: advice running at "+thisJoinPoint.getSourceLocation());
		proceed(s);
		proceed(s);
	}

	void around(Object f): unlock() && args(f) {
		System.err.println("around() unlock: advice running at "+thisJoinPoint.getSourceLocation());
		proceed(s);
		proceed(s);
	}
	
	public static void main(String[] args) {
		Foo aFoo = new Foo();
		aFoo.staticM();
		aFoo.nonstaticM();
	}
	
	static class Foo {
		public void nonstaticM() {
			synchronized (this) {
				System.err.println("non-static method running");
			}
		}
		public static void staticM() {
			synchronized (String.class) {
				System.err.println("static method running");
			}
		}
	}
}