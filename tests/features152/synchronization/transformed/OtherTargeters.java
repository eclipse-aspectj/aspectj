public class OtherTargeters {
	public static void main(String[] args) {
		new OtherTargeters().foo();
	}
	
	// This method will have branch instructions that target a return which must be
	// adjusted to target the monitor exit block
	public synchronized void foo() {
		int i = 35;
		if (i==35) {
			System.err.println("foo() running");
		}
	}

	public void goo() {
		int i = 35;
		if (i==35) {
			System.err.println("goo() running");
		}
	}
}

aspect X {
	before(): execution(* foo(..)) {System.err.println("advice running");}
}