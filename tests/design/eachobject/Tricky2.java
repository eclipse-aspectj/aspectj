import org.aspectj.testing.Tester;
import org.aspectj.runtime.NoAspectException;

public class Simple {
    public static void main(String[] args) {
	C c = new C();
	c.m();
	// aspect A is bound to the base class C
	Tester.checkEqual(c.history.toString(), "A.before, C.m, ", "C");

	SubC1 subc1 = new SubC1();
	subc1.m();
	// aspect A is also bound to the subclass SubC1
	//Tester.checkEqual(subc1.history.toString(), "A.before, SubC1.m, C.m, ", "SubC1");

	SubC2 subc2 = new SubC2();
	subc2.m();
	// aspect A is overriden by SubA on the class SubC2
	//Tester.checkEqual(subc2.history.toString(), "SubA.before, A.before, C.m, ", "SubC2");


	// retrieving the SubA aspect using its super's aspectOf static method
	//Tester.check(A.aspectOf(subc2) instanceof SubA, "A.aspectOf(subc2) instanceof SubA");

	// retrieving the SubA aspect using its own aspectOf method
	//Tester.check(SubA.aspectOf(subc2) instanceof SubA, "SubA.aspectOf(subc2) instanceof SubA");

	try {
	    // trying to retrieve a SubA aspect where only an A aspect is present
	    SubA subA = SubA.aspectOf(c);
	    Tester.checkFailed("SubA.aspectOf(c) should throw an exception");
	} catch (NoAspectException nae) {
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


abstract aspect A {
    //StringBuffer history = null;
    after (Object object) returning (): instanceof(object) && receptions(new(..)) {
	//history = c.history;
	System.out.println("created a: " + object + " on a " + this);
    }

    abstract pointcut targets();

    before (): targets() {
       System.out.println("A.before, ");
    }
}

aspect SubA extends A of eachobject(instanceof(C)) {
    pointcut targets(): receptions(void m());
}
