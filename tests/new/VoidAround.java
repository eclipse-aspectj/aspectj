
import org.aspectj.testing.Tester;
import java.util.*;

/** @testcase PR#836 void around advice without proceed */
public class VoidAround {

    public static void main(String[] args) {
        C c = new C();
        c.run();
        Object o = c.result();
        Tester.check(o == C.EXPECTED, "o: " + o);
        Tester.checkAllEvents();
    }
}

class C {
    static Object EXPECTED = new Object();
    public void run() { }
    public Object result() { return EXPECTED; }
}

aspect A {
    static {
        Tester.expectEvent("void C.run()");
        Tester.expectEvent("Object C.result()");
    }
    // no compile error expected (also note: message jp signatures are wrong?)
    void around() : target(C) && call(* r*(..)) {  // CE can't apply to methods returning Object
        Tester.event(thisJoinPoint.getSignature().toString());
    }
}
