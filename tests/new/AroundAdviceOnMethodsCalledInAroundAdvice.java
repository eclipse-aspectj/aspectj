import org.aspectj.testing.Tester; 

public class AroundAdviceOnMethodsCalledInAroundAdvice {
    public static void main(String[] args) {
        new AroundAdviceOnMethodsCalledInAroundAdvice().realMain(args);
    }
    public void realMain(String[] args) {
        Expect.expect();
        new C().g();
        new C().f("AroundMetaJoinpoints.realMain");
        new C("C.package.constructor");
        new C(123);
        new D().g();
        new D("D.package.constructor");
        new D(123);
        Tester.checkAllEvents();
    }
}


 
aspect A {
 
    Object around(): call(public Object f(String)) {
        jp(thisJoinPoint);
        return proceed();
    }
    
    private void jp (Object join) {}
    
    void around(): call(* jp(..)) {
        proceed();
    }
    
}

class D {
    Object o = new C().f("D.o");
    public D() { new C().f("D.public.constructor"); }
    D(String f) { new C().f(f); }
    protected D(int i) { new C().f("D.protected.constructor"); }
    private D(Integer i) { new C().f("D.private.constructor"); }
    { new C().f("D.init"); }
    static {
        new D(new Integer(123));
        new C().f("D.static");
    }
    public void g() {
        Tester.event("D.g");
        new C().f("D.g");
    }
}

class C {
    public C() { f("C.public.constructor"); }
    C(String f) { f(f); }
    protected C(int i) { f("C.protected.constructor"); }
    private C(Integer i) { f("C.private.constructor"); }
    Object o = f("C.o");
    static { new C(new Integer(123)).f("C.static"); }
    { f("C.init"); }
    public void g() {
        Tester.event("C.g");
        f("C.g");
    }
    public Object f(String f) {
        Tester.event("C.f-"+f);
        return null;
    }
}
class Expect {
    static void expect() {
        Tester.expectEvent("C.f-C.o");
        Tester.expectEvent("C.f-C.init");
        Tester.expectEvent("C.f-C.private.constructor");
        Tester.expectEvent("C.f-C.static");
        Tester.expectEvent("C.f-C.o");
        Tester.expectEvent("C.f-C.init");
        Tester.expectEvent("C.f-C.public.constructor");
        Tester.expectEvent("C.g");
        Tester.expectEvent("C.f-C.g");
        Tester.expectEvent("C.f-C.o");
        Tester.expectEvent("C.f-C.init");
        Tester.expectEvent("C.f-C.public.constructor");
        Tester.expectEvent("C.f-AroundMetaJoinpoints.realMain");
        Tester.expectEvent("C.f-C.o");
        Tester.expectEvent("C.f-C.init");
        Tester.expectEvent("C.f-C.package.constructor");
        Tester.expectEvent("C.f-C.o");
        Tester.expectEvent("C.f-C.init");
        Tester.expectEvent("C.f-C.protected.constructor");
        Tester.expectEvent("C.f-C.o");
        Tester.expectEvent("C.f-C.init");
        Tester.expectEvent("C.f-C.public.constructor");
        Tester.expectEvent("C.f-D.o");
        Tester.expectEvent("C.f-C.o");
        Tester.expectEvent("C.f-C.init");
        Tester.expectEvent("C.f-C.public.constructor");
        Tester.expectEvent("C.f-D.init");
        Tester.expectEvent("C.f-C.o");
        Tester.expectEvent("C.f-C.init");
        Tester.expectEvent("C.f-C.public.constructor");
        Tester.expectEvent("C.f-D.private.constructor");
        Tester.expectEvent("C.f-C.o");
        Tester.expectEvent("C.f-C.init");
        Tester.expectEvent("C.f-C.public.constructor");
        Tester.expectEvent("C.f-D.static");
        Tester.expectEvent("C.f-C.o");
        Tester.expectEvent("C.f-C.init");
        Tester.expectEvent("C.f-C.public.constructor");
        Tester.expectEvent("C.f-D.o");
        Tester.expectEvent("C.f-C.o");
        Tester.expectEvent("C.f-C.init");
        Tester.expectEvent("C.f-C.public.constructor");
        Tester.expectEvent("C.f-D.init");
        Tester.expectEvent("C.f-C.o");
        Tester.expectEvent("C.f-C.init");
        Tester.expectEvent("C.f-C.public.constructor");
        Tester.expectEvent("C.f-D.public.constructor");
        Tester.expectEvent("D.g");
        Tester.expectEvent("C.f-C.o");
        Tester.expectEvent("C.f-C.init");
        Tester.expectEvent("C.f-C.public.constructor");
        Tester.expectEvent("C.f-D.g");
        Tester.expectEvent("C.f-C.o");
        Tester.expectEvent("C.f-C.init");
        Tester.expectEvent("C.f-C.public.constructor");
        Tester.expectEvent("C.f-D.o");
        Tester.expectEvent("C.f-C.o");
        Tester.expectEvent("C.f-C.init");
        Tester.expectEvent("C.f-C.public.constructor");
        Tester.expectEvent("C.f-D.init");
        Tester.expectEvent("C.f-C.o");
        Tester.expectEvent("C.f-C.init");
        Tester.expectEvent("C.f-C.public.constructor");
        Tester.expectEvent("C.f-D.package.constructor");
        Tester.expectEvent("C.f-C.o");
        Tester.expectEvent("C.f-C.init");
        Tester.expectEvent("C.f-C.public.constructor");
        Tester.expectEvent("C.f-D.o");
        Tester.expectEvent("C.f-C.o");
        Tester.expectEvent("C.f-C.init");
        Tester.expectEvent("C.f-C.public.constructor");
        Tester.expectEvent("C.f-D.init");
        Tester.expectEvent("C.f-C.o");
        Tester.expectEvent("C.f-C.init");
        Tester.expectEvent("C.f-C.public.constructor");
        Tester.expectEvent("C.f-D.protected.constructor");
    }
}
