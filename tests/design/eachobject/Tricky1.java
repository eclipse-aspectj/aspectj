import org.aspectj.testing.Tester;

/**
 * test time of creation and existence of aspect of eachobject(P)
 * where P is not instanceof
 */
public class Tricky1 {
    public static void main(String[] args) {
	// the ReceptionAspect isn't created until just before the m message is received
	C c = new C();
	c.foo();
	c.m();
        Tester.checkEqual(c.history.toString(), "C.foo, SubReceptionAspect1.before, C.m, ");
	Tester.check(!SubReceptionAspect2.hasAspect(c), "!Sub2.hasAspect");

	// if m1 is called first, then a single SubReceptionAspect is created instead
	// no matter how many times m1 is called
	c = new C();
	c.m1();
	c.m1();
        Tester.checkEqual(c.history.toString(), 
			   "SubReceptionAspect2.before, C.m1, SubReceptionAspect2.before, C.m1, ");
	Tester.check(!SubReceptionAspect1.hasAspect(c), "!Sub1.hasAspect");


	// calling both m and m1 should prduce one of each aspect
	c = new C();
	c.m();
	c.m1();
	Tester.check(SubReceptionAspect1.hasAspect(c), "Sub1.hasAspect");
	Tester.check(SubReceptionAspect2.hasAspect(c), "Sub2.hasAspect");


	// be sure nothing unexpected happens on subtypes
	c = new SubC();
	c.foo();
	c.m();
        Tester.checkEqual(c.history.toString(), "C.foo, SubReceptionAspect1.before, SubC.m, ");

    }
}

class C {
    // records what methods are called and advice are run
    public StringBuffer history = new StringBuffer();

    public void m() {
	history.append("C.m, ");
    }
    public void m1() {
	history.append("C.m1, ");
    }

    public void foo() {
	history.append("C.foo, ");
    }
}

class SubC extends C {
    public void m() {
	history.append("SubC.m, ");
    }
}


abstract aspect ReceptionAspect perthis(targets()) {
    abstract pointcut targets();

    protected void noteBefore(C c) {
	c.history.append("ReceptionAspect.before, ");
    }

    before (C c): target(c) && execution(* *(..)) {
	noteBefore(c);
    }
}


aspect SubReceptionAspect1 extends ReceptionAspect {
    pointcut targets(): execution(void m());

    protected void noteBefore(C c) {
	c.history.append("SubReceptionAspect1.before, ");
    }
}



aspect SubReceptionAspect2 extends ReceptionAspect {
    pointcut targets(): execution(void m1());

    protected void noteBefore(C c) {
	c.history.append("SubReceptionAspect2.before, ");
    }
}
