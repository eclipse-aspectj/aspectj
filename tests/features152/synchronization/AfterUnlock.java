// after advice and unlock

public aspect AfterUnlock {

	after(Foo f): unlock() && this(f) {
		System.err.println("after(Foo) unlock: advice running at "+thisJoinPoint.getSourceLocation());
	}
	
	after(): unlock() {
		System.err.println("after() unlock: advice running at "+thisJoinPoint.getSourceLocation());
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