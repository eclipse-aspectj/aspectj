import org.aspectj.testing.Tester;

public class AroundChangeThis {
    public static void main(String[] args) {
        C c1 = new C("c1");
        C c2 = new C("c2");
        SubC sc = new SubC("sc");

        c1.m(c2);
        Tester.checkAndClearEvents(new String[] { "c1.m(c2)", "c2.m(c1)" });

        c1.m(sc);
        Tester.checkAndClearEvents(new String[] { "c1.m(sc)", "sc.m(c1)" });

//		this is the 1.3 behaviour
//        sc.m(c1);
//        Tester.checkAndClearEvents(new String[] { "sc.m(c1)", "c1.m(sc)" });

		try {
			// the 1.4 behaviour is....
			// in byte code we have a call to SubC.m
			sc.m(c1);
			Tester.checkFailed("Expected ClassCastException");
		} catch (ClassCastException e) {
		}
		
        try {
            sc.m1(c1);
			Tester.checkFailed("Expected ClassCastException");
        } catch (ClassCastException e) {
        }

        Tester.printEvents();
    }
}

class C {
    private String name;

    public C(String name) { this.name = name; }

    public String toString() { return name; }

    public void m(Object other) {
        Tester.event(this + ".m(" + other + ")");
    }
}

class SubC extends C {
    public SubC(String name) { super(name); }

    public void m1(Object other) {
        Tester.event(this + ".m1(" + other + ")");
    }
}


aspect A {
    /* Swaps this with arg for calls of C.m(C) */
    void around(C thisC, C argC): execution(void m*(*)) && this(thisC) && args(argC) {
        proceed(argC, thisC);
        proceed(thisC, argC);
    }


    void around(C thisC, C argC): call(void m*(*)) && target(thisC) && args(argC) {
        proceed(argC, thisC);
    }
}
