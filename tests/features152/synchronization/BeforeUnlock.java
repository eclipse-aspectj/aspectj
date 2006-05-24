// before advice and unlock

public aspect BeforeUnlock {

	before(Foo f): unlock() && this(f) {
		System.err.println("before(Foo) unlock: advice running at "+thisJoinPoint.getSourceLocation());
	}
	
	before(): unlock() {
		System.err.println("before() unlock: advice running at "+thisJoinPoint.getSourceLocation());
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