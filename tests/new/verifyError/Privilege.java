
import org.aspectj.testing.Tester;

class C {
	private int i;
}

privileged aspect A {
	private int C.j = 1;
}

/** @testcase PR#36673 privileged aspect main verify error */
public privileged aspect Privilege {
	public static void main(String[] args) {
	    C c = new C();
		Tester.check(1 == c.j, "wrong value for c.j");
	}	
}

