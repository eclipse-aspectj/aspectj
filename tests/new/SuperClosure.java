
public class SuperClosure {
	public static void main(String[] args) {
	}
}

aspect A {
	void around() : execution(void main(String[])) {
		Runner runner = new Runner() {
			public void run() {
				// ajc 1.1.1 VerifyError: Illegal use of nonvirtual function call
	           super.run();
			}
		};
		runner.run();
	}
}
class Runner implements Runnable {
	public void run() {
	}
}
