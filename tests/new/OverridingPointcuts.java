import org.aspectj.testing.Tester;

public class OverridingPointcuts {
    public static void main(String[] args) {
        C c1 = new C();
        C c2 = new C();

        c1.m1();
        c2.m2();

        Tester.checkEqual(c1.m1(), "A1-m1");
        Tester.checkEqual(c2.m2(), "m2");

        Tester.checkEqual(c1.m3(), "A1-m3");
        Tester.checkEqual(c2.m3(), "m3");
	
        Tester.check(!A2.hasAspect(c1), "c1 hasa A2");
        Tester.check(A1.hasAspect(c1), "c1 hasa A1");

        Tester.check(A2.hasAspect(c2), "c2 hasa A2");
        Tester.check(!A1.hasAspect(c2), "!c2 hasa A1");
    }
}


class C {
    public String m1() { return "m1"; }
    public String m2() { return "m2"; }
    public String m3() { return "m3"; }
}

abstract aspect A pertarget(target(C) && call(String m2())) {
    int cached;
    
    abstract pointcut testpoint();
    
    String modifyResult(String r) { return "A-" + r; }

    String around (): testpoint() {
        return modifyResult(proceed());
    }
}

aspect A1 extends A pertarget(target(C) && call(String m1())) {
    pointcut testpoint() : call(String C.*(..));

    String modifyResult(String s) { return "A1-" + s; }
}

aspect A2 extends A {
    pointcut testpoint();
}

/*
public aspect A2 extends A of eachobject(instanceof(C) && receptions(String m2())) {
    pointcut testpoint() returns String: receptions(String *(..));

    String modifyResult(String s) { return "A2-" + s; }
}
*/
