import org.aspectj.testing.Tester;
import org.aspectj.testing.Tester; 
public class BackdoorMethods {
    public static void main(String[] args) {
        new BackdoorMethods().realMain(args);
    }
    public void realMain(String[] args) {
        new _A().a();
        new _B().a();
        new _C().a();
        new _D().a();
        Tester.checkAllEvents();
    }
    static {
        for (char c = 'A'; c <= 'D'; c++) {
            String n = "_"+c+".";
            Tester.expectEvent(n+"a");
            if (c != 'D') Tester.expectEvent(n+"f");
            Tester.expectEvent(n+"g");
            Tester.expectEvent("before."+n+"a");
            if (c != 'D') Tester.expectEvent("before."+n+"g");
        }
    }
}

class O {
    public void a() { Tester.event(n("a")); }
    protected String n() { return getClass().getName() + "."; }
    protected String n(Object s) { return n() + s; }
}
class A extends O  { public    void f() { Tester.event(n("f")); } }
class B extends O  {           void f() { Tester.event(n("f")); } }
class C extends O  { protected void f() { Tester.event(n("f")); } }
class D extends O  { private   void f() { Tester.event(n("f")); } }

class _A extends O { public    void g() { Tester.event(n("g")); } }
class _B extends O {           void g() { Tester.event(n("g")); } }
class _C extends O { protected void g() { Tester.event(n("g")); } }
class _D extends O { private   void g() { Tester.event(n("g")); } }

privileged aspect Aspect {
    
    declare parents: _A extends A;
    declare parents: _B extends B;
    declare parents: _C extends C;
    declare parents: _D extends D;

    before(_A o): target(o) && call(void g()) { o.f(); n(o,"g"); }
    before(_B o): target(o) && call(void g()) { o.f(); n(o,"g"); }
    before(_C o): target(o) && call(void g()) { o.f(); n(o,"g"); }

    before(_A o): target(o) && call(void a()) { o.g(); n(o,"a"); }
    before(_B o): target(o) && call(void a()) { o.g(); n(o,"a"); }
    before(_C o): target(o) && call(void a()) { o.g(); n(o,"a"); }
    before(_D o): target(o) && call(void a()) { o.g(); n(o,"a"); }

    private static void n(Object o, String m) {
        Tester.event("before." + o.getClass().getName() + "." + m);
    }
}


