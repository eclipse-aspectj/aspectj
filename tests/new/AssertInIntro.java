import org.aspectj.testing.Tester;

public class AssertInIntro {

    public static void main(String[] args) {
	turnOnAssertions();
	runTests();
    }

    static void turnOnAssertions() {
	ClassLoader cl = AssertInIntro.class.getClassLoader();
	cl.setClassAssertionStatus("C", false);
	cl.setClassAssertionStatus("A", true);
    }

    static void runTests() {
	boolean failed = false;
	try {
	    C.foo();
	} catch (AssertionError e) {
	    failed = true;
	}
	Tester.check(failed, "introduced assertions improperly off");

	failed = false;
	try {
	    C.goo();
	} catch (AssertionError e) {
	    failed = true;
	}
	Tester.check(!failed, "non-introduced assertions improperly on");
    }
}

class C {
    static void goo() {
	assert false;
    }
}

aspect A {
    static void C.foo() {
	assert false;
    }
}
