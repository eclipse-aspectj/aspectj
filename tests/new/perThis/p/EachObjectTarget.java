package p;
import org.aspectj.testing.Tester;

public class EachObjectTarget {
	public static void main(String[] args) {
		EachObjectTarget o = new EachObjectTarget();
		o.foo();
	}
	
	void foo() {
		Tester.check(true, "Dummy test");
	}
}
