import org.aspectj.testing.Tester;

// we want to be doing tests based on dynamic type in if
public class StaticTypeInIf {
    public static void main(String[] args) {
	D d = new D();
	C c = new C();

	c.foo(c);
        Tester.checkAndClearEvents(new String[] {});

        A.setF(d, false);
	c.foo(d);
        Tester.checkAndClearEvents(new String[] {});
        A.setF(d, true);
	c.foo(d);
        Tester.checkAndClearEvents(new String[] {"args"});

        A.setF(d, false);
	d.foo(c);
        Tester.checkAndClearEvents(new String[] {});
        A.setF(d, true);
	d.foo(c);
        Tester.checkAndClearEvents(new String[] {"this", "target"});

        A.setF(d, false);
	d.foo(d);
        Tester.checkAndClearEvents(new String[] {});
        A.setF(d, true);
	d.foo(d);
        Tester.checkAndClearEvents(new String[] {"args", "this", "target"});

    }

}

class C {
    void foo(C c) {}
}

class D extends C {
    void foo(C c) {}
}


aspect A {
    private boolean D.f = false;

    static void setF(D d, boolean b) { d.f = b; }

    pointcut foo(D d): call(void C.foo(C)) && target(d) && if(d.f); 

    before (C c): foo(c) {
        Tester.event("target");
    }

    before (D d): if(d.f) && call(void C.foo(C)) && args(d) {
        Tester.event("args");

    }

    before (D d): execution(void C.foo(C)) && this(d) && if(d.f) {
        Tester.event("this");
    }
}
