import org.aspectj.testing.*;

public strictfp class StrictFpCompile {
    public static void main(String[] args) {
        new StrictFpCompile().go();
        Tester.check(ran, "go did not run");
    }

    static boolean ran = false;

    void go() {
        ran = true;
    }
}


// Ok, must be generated with strictfp modifier
strictfp interface StrictInterface {
	// Has to be error, may not generate strictfp, but has to set strictfp in bytecode
	// strictfp float test1();
	
	// Ok, may not be generated with strictfp modifier
	float test2();
};

// Ok, must be generated with strictfp modifier
strictfp abstract class StrictClass {
	// Has to be an error
	// strictfp float f;
	
	// Ok
	double d;
	
	// Has to be error, may not generate strictfp, but has to set strictfp in bytecode
	// strictfp StrictClass() {}
	
	// Ok, must not generate strictfp, but has to set strictfp in bytecode
	StrictClass(double _d) { d = _d; }

	// Ok, may be generated with strictfp modifier
	abstract float test1();
	
	// Ok, may be generated with strictfp modifier
	float test2() { return 0.f; }
	
	// Ok, may be generated with strictfp modifier
	strictfp float test3() { return 0.f; }

	// Ok, may be generated with strictfp modifier
	strictfp static float test4() { return 0.f; }

};

// Ok, may not be generated with strictfp modifier
class NonStrictClass {
	// Ok
	NonStrictClass() {}
	
	// Ok, may not be generated with strictfp modifier
	float test2() { return 0.f; }
	
	// Ok, must be generated with strictfp modifier
	strictfp float test3() { return 0.f; }

	// Ok, must be generated with strictfp modifier
	strictfp static float test4() { return 0.f; }

};

// Ok
strictfp class OuterStrictClass {
	
	// Ok, may be generated with strictfp modifier
	class InnerStrictClass {
	
		// Ok, may be generated with strictfp modifier
		class InnerInnerClass {
		}
	}

};

