import org.aspectj.testing.Tester; 
public class AfterAdviceOnConstructorsOnTheWrongType {
    public static void main(String[] args) {
        new AfterAdviceOnConstructorsOnTheWrongType().realMain(args);
    }
    public void realMain(String[] args) {
        new C().c();
        new D().d();
        Tester.checkAllEvents();
    }
    static {
        Tester.clearEvents();
        // new(..) for both class and interface
        Tester.expectEventsInString("after-c,after-c,c,after-d,after-d,d");
    }
}

interface I {}
class C implements I { public void c() { Tester.event("c"); } }
class D implements I { public void d() { Tester.event("d"); } }

aspect A {
    after(C c): target(c) && execution(new(..)) {
        Tester.event("after-c");
    }
    after(D d): target(d) && execution(new(..)) {
        Tester.event("after-d");
    }
}
