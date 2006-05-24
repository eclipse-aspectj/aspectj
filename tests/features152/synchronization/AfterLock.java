// after advice and lock

public aspect AfterLock {

	after(Foo f): lock() && this(f) {
		System.err.println("after(Foo) lock: advice running at "+thisJoinPoint.getSourceLocation());
	}
	
	after(): lock() {
		System.err.println("after() lock: advice running at "+thisJoinPoint.getSourceLocation());
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