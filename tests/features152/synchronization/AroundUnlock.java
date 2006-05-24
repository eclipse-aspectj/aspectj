// around advice and unlock

public aspect AroundUnlock {

	void around(Foo f): unlock() && args(f) {
		System.err.println("around(Foo) lock: advice running at "+thisJoinPoint.getSourceLocation());
		proceed(f);
	}
	
	void around(): unlock() {
		System.err.println("around() lock: advice running at "+thisJoinPoint.getSourceLocation());
		proceed();
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