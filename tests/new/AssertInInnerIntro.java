// compile this guy with -usejavac to show warning

public class AssertInInnerIntro {

    public static void main(String[] args) {
	turnOnAssertions();
	runTests();
    }

    static void turnOnAssertions() {
	ClassLoader cl = AssertInInnerIntro.class.getClassLoader();
	cl.setClassAssertionStatus("C", false);
	cl.setClassAssertionStatus("A", true);
    }

    static void runTests() {
	// should throw assertion error, will not
	C.foo();
    }
}

class C {
}

aspect A {
    static void C.foo() {
	new Runnable() {
	    public void run() {
		assert false;
	    }
	}.run();
    }
}
