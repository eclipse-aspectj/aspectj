// unlock and non-static context

public aspect ParsingAndMatching4 {

	before(): unlock() {
		System.err.println("Advice running at "+thisJoinPoint.getSourceLocation());
	}
	
	public static void main(String[] args) {
		new Foo().nonstaticM();
	}
	
	static class Foo {
		public void nonstaticM() {
			synchronized (String.class) {
				System.err.println("non-static method running");
			}
		}
	}
}