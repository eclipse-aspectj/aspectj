import org.aspectj.testing.Tester; 
public class ParsingSubtypesIntroductions {
    public static void main(String[] args) {
        new ParsingSubtypesIntroductions().realMain(args);
    }
    public void realMain(String[] args) {
        new D().f();
        new C().f();
        ((I)new C()).i();
        new E().f();
        ((I)new E()).i();
        Tester.checkAllEvents();
    }
    static {
        U.m(D.class, "f");
        U.m(C.class, "f");
        U.m(C.class, "i");
        U.m(E.class, "f");
        U.m(E.class, "i");
    }
}

class U {
    public static void a(Object o, Object m) {
        Tester.event(m + "." + type(o));
    }
    public static void m(Class t, Object m) {
        Tester.expectEvent(m + "." + name(t));
    }
    public static String type(Object o) {
        return name(o.getClass());
    }
    public static String name(Class t) {
        String str = t.getName();
        int i = str.lastIndexOf('.');
        if (i != -1) str = str.substring(i+1);
        return str;
    }
}

class D {
    public void f() { U.a(this, "f"); }
}
class C /*extends D implements I*/ {
    /*public void i() { U.a(this, "i"); }*/
}
class E extends C {}
interface I {
    public void i();
}
aspect A {
//      subtypes(C) +implements I;
//      subtypes(C) +extends D;

    declare parents: C+ implements I;
    declare parents: C+ extends    D;

    public void I.i() { U.a(this, "i");  }
}

