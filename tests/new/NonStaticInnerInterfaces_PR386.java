import org.aspectj.testing.*;

public class NonStaticInnerInterfaces_PR386 {
    public static void main(String[] args) {
        A a = new A();
        A.I ab = new C();
        C c = new C();
        a.go();
        ab.go();
        c.go();
        Tester.checkAllEvents();
    }
    static {
        Tester.expectEventsInString("A0,C1,C2,");
    }
}

class B {
    static int j = 0;
}

class A {
    public interface I { void go(); }
    public void go() { Tester.event("A" + (B.j++)); }
        
}

class C extends A implements I {
    public void go() { Tester.event("C" + (B.j++)); }
}

