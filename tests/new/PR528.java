import org.aspectj.testing.Tester; 
import org.aspectj.testing.Tester;

/** 
 * @testcase PR#528 10rc1 error in return type (verify error if -usejavac, compile error (missing return value) otherwise)
 * @testcase PR#528 10a1 unimplemented method if around advice/cflow on methods introduced by interface
 *
 * The !cflow(within(B)) winds up being the best test case so far for
 * the ExceptionInInitializer bug with null fields for cflow state
 *
 */
public class PR528 {
    public static void main(String[] args) {
        C c = new C();
        c.trigger();   // toggled to true, do callback
        c.trigger();   // toggled to false, do trigger
        Tester.checkAllEvents();
    }
    static {
        Tester.expectEvent("test");
        Tester.expectEvent("test"); // called for each trigger
        Tester.expectEvent("callback");
        Tester.expectEvent("trigger");
        Tester.expectEvent("around 0");
        Tester.expectEvent("around 1");
    }
}
class C {
}

abstract aspect A {
    static boolean toggle;
    static int originalIndex;
    static int callbackIndex;
    static int aroundIndex;
    interface I {
    }

    public boolean I.test() {
        Tester.event("test");
        return (toggle = !toggle);
    }

    public void I.trigger() {
        Tester.event("trigger");
        Tester.check(0==originalIndex, "trigger called again: ");
        originalIndex++;
    }

    public void I.callback() {
        Tester.event("callback");
        Tester.check(0==callbackIndex, "callback called again: ");
        callbackIndex++;
    }

    declare parents: C implements I;
}

aspect B extends A {
    void around(I i)
        : target(i) 
        && execution(public void I.trigger()) 
        && !cflow(within(B)) {
        Tester.event("around " + aroundIndex++);
        if(i.test()) {
            i.callback();
        } else { 
            proceed(i); 
        }
    }
}
