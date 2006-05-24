// unlock and static context

public aspect ParsingAndMatching2 {

	before(): unlock() {
		System.err.println("Advice running at "+thisJoinPoint.getSourceLocation());
	}
	
	public static void main(String[] args) {
		staticM();
	}
	
	public static void staticM() {
		synchronized (String.class) {
			System.err.println("static method running");
		}
	}
}