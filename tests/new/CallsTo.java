import org.aspectj.testing.Tester;

public class CallsTo {
    public static void main(String[] args) {
        Tester.clearNotes();
        //System.out.println("cleared events");
        //Tester.printEvents();
	C c = new C();
	Tester.checkAndClear
            ("ca-newC",
             "new C");
	c.m("a");
	Tester.checkAndClear
            ("re-all re-C ca-all ca-C ",
             "c.m");
	c.m_C("b");
	Tester.checkAndClear
            ("re-all re-C ca-all ca-C ",
             "c.m_C");

	c = new SubC();
	Tester.checkAndClear
            ("ca-newC",
             "new SubC");
	c.m("c");
	Tester.checkAndClear
            ("re-all re-C re-SubC ca-all ca-C ca-SubC ca-inst(SubC)",
             "subc.m");
	c.m_C("d");
	Tester.checkAndClear
            ("re-all re-C re-SubC ca-all ca-C ca-SubC ca-inst(SubC)",
             "subc.m_C");

	SubC subC = new SubC();
	Tester.checkAndClear
            ("ca-newC",
             "new SubC");
	subC.m("e");
	Tester.checkAndClear
            ("re-all re-C re-SubC ca-all ca-C ca-SubC ca-inst(SubC)",
             "subc.m");
	subC.m_C("f");
	Tester.checkAndClear
            ("re-all re-C re-SubC ca-all ca-C ca-SubC ca-inst(SubC)",
             "subc.m_C");
	subC.m_SubC();
	Tester.checkAndClear
            ("re-all re-SubC ca-all ca-SubC",
             "subc.m_SubC");
    }
}

class C {
    public void m(String s1) {}
    public void m_C(String s2) {}
}

class SubC extends C {
    public void m(String s3) {}
    public void m_SubC() {}
}

aspect A {
//      pointcut allReceptions(): receptions(void *(..)) && instanceof(C);
//      pointcut receptionsInC(String s): receptions(void C.*(s));
//      pointcut receptionsInSubC(): receptions(void SubC.*(..));

    pointcut allcall():         call(void *(..)) && target(C+);
    pointcut callInC(String s): call(void C+.*(String)) && args(s);
    pointcut callInSubC():      call(void *(..)) && target(SubC);

    before():            allcall() { note("re-all"); }
    before(String s):   callInC(s) { note("re-C"); }
    before():         callInSubC() { note("re-SubC"); }

    before():            allcall() { note("ca-all"); }
    before(String s):   callInC(s) { note("ca-C"); }
    before():         callInSubC() { note("ca-SubC"); }


    before(): call(void C.*(..)) && target(SubC) { note("ca-inst(SubC)"); }

    // not implemented
//      before(OnSubC o): call(void C.*(..)) && (hasaspect(OnSubC) && target(o)) {
//  	note("ca-asp(SubC)");
//      }

    before(): call(C+.new(..)) { note("ca-newC"); }

    private final static void note(String msg) {
        Tester.note(msg);
    }
}

aspect OnSubC /**of eachobject(instanceof(SubC))*/ {
}
