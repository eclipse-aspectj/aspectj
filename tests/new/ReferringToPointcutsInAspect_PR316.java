import org.aspectj.testing.Tester; 
public class ReferringToPointcutsInAspect_PR316 {
    public static void main(String[] args) {
        new C().f();
        Tester.checkAllEvents();
    }
    static {
        Tester.expectEvent("void-f");
        Tester.expectEvent("before-f");
    }
}

class C {
    public void f() { Tester.event("void-f"); }
}

aspect A /*of eachobject(i())*/ {
    pointcut i(): target(C);
    before(): i() && execution(* f(..)) { Tester.event("before-f"); }
}
