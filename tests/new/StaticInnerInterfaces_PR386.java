import org.aspectj.testing.*;

public class StaticInnerInterfaces_PR386 {
    public static void main(String[] args) {
        A a = new A();
        A.I ab = new C();
        C c = new C();
        D d = new D();
        a.go();
        ab.go();
        c.go();
        d.go();
        Tester.checkAllEvents();
    }
    static {
        Tester.expectEventsInString("A0,C1,C2,D3");
    }
}

class B {
    static int j = 0;
}

class A {
    public static interface I { void go(); }
    public void go() { Tester.event("A" + (B.j++)); }
        
}

class C extends A implements I {
    public void go() { Tester.event("C" + (B.j++)); }
}

class D implements A.I {
    public void go() { Tester.event("D" + (B.j++)); }
}
