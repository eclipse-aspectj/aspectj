
import org.aspectj.testing.Tester;

class C {
	private int i;
}

privileged aspect A {
	private int C.j = 1;
	private static String C.s = "hello";
	
	private String C.m() {
		return "from A";
	}
}

/** @testcase PR#36673 privileged aspect main verify error */
public privileged aspect Privilege {
	public static void main(String[] args) {
	    C c = new C();
		Tester.checkEqual(1, c.j, "wrong value for c.j");
		Tester.checkEqual("hello", C.s, "wrong value for C.s");
		Tester.checkEqual("from A", c.m(), "c.m()");
	}	
}

