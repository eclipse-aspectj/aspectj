// Subject to LTW

public class BasicProgram1 {
	
	public static void main(String[] args) {
		new BasicProgram1().nonstaticM();
		staticM();
	}
	
	public static void staticM() {
		synchronized (String.class) {
			System.err.println("static method running");
		}
	}
	
	public void nonstaticM() {
		synchronized (this) {
			System.err.println("nonstatic method running");
		}
	}
}