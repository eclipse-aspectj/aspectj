
import org.aspectj.testing.Tester;
import java.util.*;

/** @testcase PR#883 Getting signature from handler join point */
public class HandlerSignature {

    public static void main(String[] args) {
        C c = new C();
        U.ee("caught");
        try {
            c.run();
        } catch (Error e) {
            U.e("caught");
        }
        Tester.checkAllEvents();
    }
    static {
        // U.ee("X");
    }
}

class C {
    public void run() {
        throw new Error("");
    }
}

class U {
    static void e(String event) {
        System.err.println(event);
        Tester.event(event);
    }
    static void ee(String event) {
        Tester.expectEvent(event);
    }
}

aspect A {
    static {
        U.ee("before handler");
        //U.ee("after handler");
    }

    before() : handler(*) {
      thisJoinPoint.getSignature().getModifiers();   
      U.e("before handler");
    }
    /*
      after returning join points not implemented
    after() returning: handler(*) {
      thisJoinPoint.getSignature().getModifiers();   
      U.e("after handler");
    }
    */
}
