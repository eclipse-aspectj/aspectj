// attempts to be a comprehensive test of assert statements that have
// only intra-package behaviour.

import org.aspectj.testing.Tester;

public class AssertInOnePackage {
    public static void main(String[] args) {
	turnOnAssertions();
	runTests();
    }

    // we first turn on assertions.  This is run _before_ any other
    // method is run, so the only classes that are loaded are
    // AssertInOnePackage.
    static void turnOnAssertions() {
	ClassLoader cl = AssertInOnePackage.class.getClassLoader();

	cl.setClassAssertionStatus("StaticInitializerOnHelper", true);
	cl.setClassAssertionStatus("StaticInitializerOffHelper", false);

	cl.setClassAssertionStatus("ConstructorOnHelper", true);
	cl.setClassAssertionStatus("ConstructorOffHelper", false);

	cl.setClassAssertionStatus("InnerStaticInitializerOnHelper", true);
	cl.setClassAssertionStatus("InnerStaticInitializerOffHelper", false);

	cl.setClassAssertionStatus("InnerStaticInitializerOnHelperI", true);
	cl.setClassAssertionStatus("InnerStaticInitializerOffHelperI", false);

	cl.setClassAssertionStatus("CycleSubOn", true);
	cl.setClassAssertionStatus("CycleSubOff", false);

    }

    // In the following tests, the assignment to the static field
    // translates into a putstatic bytecode, which, by section 5.5 of
    // the JVM spec, will initialize the class.
    static void runTests() {

	check(true,
	      "static initializer should throw",
	      new Runnable() {
		  void run() { StaticInitializerOnHelper.x = 3; }
	      });
	check(false,
	      "static initializer should not throw",
	      new Runnable() {
		  void run() { StaticInitializerOffHelper.x = 3; }
	      });

	check(true,
	      "constructor should throw",
	      new Runnable() {
		  void pre() { ConstructorOnHelper.x = 3; }
		  void run() { new ConstructorOnHelper(); }

	      });
	check(false,
	      "static initializer should not throw",
	      new Runnable() {
		  void pre() { ConstructorOffHelper.x = 3; }
		  void run() { new ConstructorOffHelper(); }
	      });

	check(true,
	      "inner static initializer should throw",
	      new Runnable() {
		  void run() { InnerStaticInitializerOnHelper.Inner.x = 3; }
	      });
	check(false,
	      "inner static initializer should not throw",
	      new Runnable() {
		  void run() { InnerStaticInitializerOffHelper.Inner.x = 3; }
	      });

	check(true,
	      "inner static initializer of interface should throw",
	      new Runnable() {
		  void run() { InnerStaticInitializerOnHelperI.Inner.x = 3; }
	      });
	check(false,
	      "inner static initializer of interface should not throw",
	      new Runnable() {
		  void run() { InnerStaticInitializerOffHelperI.Inner.x = 3; }
	      });


	check(true,
	      "static initializer in cyclic should throw",
	      new Runnable() {
		  void run() { CycleSubOn.x = 3; }
	      });
	check(true,
	      "static initializer in cyclic should always throw",
	      new Runnable() {
		  void run() { CycleSubOff.x = 3; }
	      });

    }


    static void check(boolean shouldThrow, String message, Runnable r) {
	r.pre();
	boolean threw = false;
	try {
	    r.run();
	} catch (AssertionError e) {
	    threw = true;
	}
	if (threw != shouldThrow) {
	    //System.err.println(message);
	    Tester.check(false, message);
	}
	r.post();
    }

    static class Runnable {
	void pre() {}
	void run() {}
	void post() {}
    }
}

// ------------------------------

// Asserts in a static initializer of a class

class StaticInitializerOnHelper {
    static int x;
    static {
	assert false;
    }
}

class StaticInitializerOffHelper {
    static int x;
    static {
	assert false;
    }
}

// ------------------------------

// Asserts in a constructor of a class.  This stands in for all
// "normal" assertion, so I'm not going to bother writing asserts in
// other post-class-initialization contexts.

class ConstructorOnHelper {
    static int x;
    ConstructorOnHelper() {
	assert false;
    }
}

class ConstructorOffHelper {
    static int x;
    ConstructorOffHelper() {
	assert false;
    }
}

// ------------------------------

// Asserts in a static initializer of an inner class.

class InnerStaticInitializerOnHelper {
    static class Inner {
	static int x;
	static {
	    assert false;
	}
    }
}

class InnerStaticInitializerOffHelper {
    static class Inner {
	static int x;
	static {
	    assert false;
	}
    }
}

// ------------------------------

// Asserts in a static initializer of an inner class of an interface.

interface InnerStaticInitializerOnHelperI {
    static class Inner {
	static int x;
	static {
	    assert false;
	}
    }
}

interface InnerStaticInitializerOffHelperI {
    static class Inner {
	static int x;
	static {
	    assert false;
	}
    }
}


// ------------------------------

// Asserts in the subclass called in an initialization inversion

class CycleSuperOn {
    static {
	CycleSubOn.foo();
    }
}
class CycleSubOn extends CycleSuperOn {
    static int x;
    static void foo() {
	assert false;
    }
}

class CycleSuperOff {
    static {
	CycleSubOff.foo();
    }
}
class CycleSubOff extends CycleSuperOff {
    static int x;
    static void foo() {
	assert false;
    }
}


