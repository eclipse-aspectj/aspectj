import org.aspectj.testing.*;
public class Params {
    public static void main(String[] args) {
        new Params().go();
    }

    void go() {
        A a = new A(); B b  = new B(); C c = new C(); D d = new D();
        b(a);
        
        b(a, b);
        b(b, a);
        
        b(a, b, c);
        b(a, c, b);
        b(b, a, c);
        b(b, c, a);
        b(c, a, b);
        b(c, b, a);
        
        b(d, a, b, c);
        b(d, a, c, b);
        b(d, b, a, c);
        b(d, b, c, a);
        b(d, c, a, b);
        b(d, c, b, a);
        
        b(a, d, b, c);
        b(a, d, c, b);
        b(b, d, a, c);
        b(b, d, c, a);
        b(c, d, a, b);
        b(c, d, b, a);
        
        b(a, b, d, c);
        b(a, c, d, b);
        b(b, a, d, c);
        b(b, c, d, a);
        b(c, a, d, b);
        b(c, b, d, a);
        
        b(a, b, c, d);
        b(a, c, b, d);
        b(b, a, c, d);
        b(b, c, a, d);
        b(c, a, b, d);
        b(c, b, a, d);
        
        Tester.checkAllEvents();
    }

    static void m_(String str) {
        Tester.expectEvent(str);
        Tester.expectEvent(str + ".advice");
    }

    static {
        m_("b1");
    
        m_("b2.1");
        m_("b2.2");
    
        m_("b3.1");
        m_("b3.2");
        m_("b3.3");
        m_("b3.4");
        m_("b3.5");
        m_("b3.6");

        m_("b4.1.1");
        m_("b4.1.2");
        m_("b4.1.3");
        m_("b4.1.4");
        m_("b4.1.5");
        m_("b4.1.6");

        m_("b4.2.1");
        m_("b4.2.2");
        m_("b4.2.3");
        m_("b4.2.4");
        m_("b4.2.5");
        m_("b4.2.6");

        m_("b4.3.1");
        m_("b4.3.2");
        m_("b4.3.3");
        m_("b4.3.4");
        m_("b4.3.5");
        m_("b4.3.6");
    
        m_("b4.4.1");
        m_("b4.4.2");
        m_("b4.4.3");
        m_("b4.4.4");
        m_("b4.4.5");
        m_("b4.4.6");
    }

    void b(A a) { a("b1"); }
    
    void b(A a, B b) { a("b2.1"); }
    void b(B b, A a) { a("b2.2"); }
    
    void b(A a, B b, C c) { a("b3.1"); }
    void b(A a, C c, B b) { a("b3.2"); }
    void b(B b, A a, C c) { a("b3.3"); }
    void b(B b, C c, A a) { a("b3.4"); }
    void b(C c, A a, B b) { a("b3.5"); }
    void b(C c, B b, A a) { a("b3.6"); }

    void b(D d, A a, B b, C c) { a("b4.1.1"); }
    void b(D d, A a, C c, B b) { a("b4.1.2"); }
    void b(D d, B b, A a, C c) { a("b4.1.3"); }
    void b(D d, B b, C c, A a) { a("b4.1.4"); }
    void b(D d, C c, A a, B b) { a("b4.1.5"); }
    void b(D d, C c, B b, A a) { a("b4.1.6"); }

    void b(A a, D d, B b, C c) { a("b4.2.1"); }
    void b(A a, D d, C c, B b) { a("b4.2.2"); }
    void b(B b, D d, A a, C c) { a("b4.2.3"); }
    void b(B b, D d, C c, A a) { a("b4.2.4"); }
    void b(C c, D d, A a, B b) { a("b4.2.5"); }
    void b(C c, D d, B b, A a) { a("b4.2.6"); }

    void b(A a, B b, D d, C c) { a("b4.3.1"); }
    void b(A a, C c, D d, B b) { a("b4.3.2"); }
    void b(B b, A a, D d, C c) { a("b4.3.3"); }
    void b(B b, C c, D d, A a) { a("b4.3.4"); }
    void b(C c, A a, D d, B b) { a("b4.3.5"); }
    void b(C c, B b, D d, A a) { a("b4.3.6"); }

    void b(A a, B b, C c, D d) { a("b4.4.1"); }
    void b(A a, C c, B b, D d) { a("b4.4.2"); }
    void b(B b, A a, C c, D d) { a("b4.4.3"); }
    void b(B b, C c, A a, D d) { a("b4.4.4"); }
    void b(C c, A a, B b, D d) { a("b4.4.5"); }
    void b(C c, B b, A a, D d) { a("b4.4.6"); }

    static String str = "";
    public static void a(Object o) {
        str = o + "";
        Tester.event(str);
    }
    
}

class A {}
class B {}
class C {}
class D {}

aspect ParamsAspect {
    pointcut intParams(): call(* *(..,A,..)) && target(Params);
    after(): intParams() {
        Tester.event(Params.str + ".advice");
    }
}
