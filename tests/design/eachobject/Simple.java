import org.aspectj.testing.Tester;
import org.aspectj.lang.NoAspectBoundException;

public class Simple {
    public static void main(String[] args) {
	C c = new C();
	c.m();
	// aspect A is bound to the base class C
	Tester.checkEqual(c.history.toString(), "A.before, C.m, ", "C");

	SubC1 subc1 = new SubC1();
	subc1.m();
	// aspect A is also bound to the subclass SubC1
	Tester.checkEqual(subc1.history.toString(), "A.before, SubC1.m, C.m, ", "SubC1");

	SubC2 subc2 = new SubC2();
	subc2.m();
	// aspect A is overriden by SubA on the class SubC2
	Tester.checkEqual(subc2.history.toString(), "SubA2.before, A.before, A.before, C.m, ", "SubC2");


	Tester.check(SubA1.aspectOf(subc2) instanceof SubA1, "SubA1.aspectOf(subc2) instanceof SubA1");

	// retrieving the SubA aspect using its own aspectOf method
	Tester.check(SubA2.aspectOf(subc2) instanceof SubA2, "Sub2A.aspectOf(subc2) instanceof SubA2");

	try {
	    // trying to retrieve a SubA aspect where only an A aspect is present
	    SubA2 subA = SubA2.aspectOf(c);
	    Tester.checkFailed("SubA2.aspectOf(c) should throw an exception");
	} catch (NoAspectBoundException nae) {
	    Tester.note("NoAspectException caught");
	}

	Tester.check("NoAspectException caught");
    }
}


class C {
    // records what methods are called and advice are run
    public StringBuffer history = new StringBuffer();

    public void m() {
	history.append("C.m, ");
    }
}

class SubC1 extends C {
    public void m() {
	history.append("SubC1.m, ");
	super.m();
    }
}

class SubC2 extends C {
}


abstract aspect A pertarget(targets()) {
    abstract pointcut targets();
    StringBuffer history = null;
    after (C c) returning (): target(c) && initialization(new(..)) {
	//System.out.println(thisJoinPoint);
	history = c.history;
    }

    before (): call(void m()) {
	history.append("A.before, ");
    }
}

aspect SubA1 extends A {
    pointcut targets(): target(C);
}


aspect SubA2 extends A {
    pointcut targets(): target(SubC2);
    before (): call(void m()) {
	history.append("SubA2.before, ");
    }
}
