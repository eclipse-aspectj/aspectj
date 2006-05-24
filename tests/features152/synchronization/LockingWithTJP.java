// obtaining the object being locked on

public aspect LockingWithTJP {

	before(): lock() {
		System.err.println("before() lock: advice running at "+thisJoinPoint.getSourceLocation());
		System.err.println("Locked on "+thisJoinPoint.getArgs()[0]);
	}
	
	public static void main(String[] args) {
		Foo aFoo = new Foo();
		aFoo.nonstaticM();
		aFoo.staticM();
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