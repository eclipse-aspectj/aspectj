import org.aspectj.testing.*;

public class DeclareAccess {
    public static void main (String[] args) {
        Tester.event("main");
        Target t = new Target();
        Aspect a = Aspect.aspectOf();
        a.tryPI(t);
        Tester.checkAllEvents();
    } 

    static {
        Tester.expectEvent("run");
        Tester.expectEvent("main");
        Tester.expectEvent("value");
    }
}

class Target {
    public String value() {
        Tester.event("run");
        return "value";
    }
}

/** @testcase private inner interface accessible in scope when declared on outer class */
aspect Aspect {
    private interface PI { 
        public String value();
    }
    public void tryPI(Target t) {
        PI pi = (PI) t;
        Tester.event(pi.value());
    }
    /** @testcase private interface declared on Target */
    declare parents:  Target implements PI;
}
