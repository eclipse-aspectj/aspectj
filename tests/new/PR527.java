import org.aspectj.testing.Tester; 

/** 
 * PR#527  bug in 1.0beta1 but not HEAD as of 9/24/01
 * @testcase compile error using pcd if() with advice on introduced methods.
 */
public class PR527 {
    public static void main(String[] args) {
        new C().run();
        Tester.checkAllEvents();
    }
    static {
        Tester.expectEvent("run");
        Tester.expectEvent("test");
        // Tester.expectEvent("trigger"); replaced in around
        Tester.expectEvent("after");
        Tester.expectEvent("callback");
        Tester.expectEvent("around");
    }
}
class C {
    public void run() {
        Tester.event("run");
    }
}

aspect A {
    interface I {
    }

    public boolean I.test() {
        Tester.event("test");
        return true;
    }

    public void I.trigger() {
        Tester.event("trigger"); // should not run
    }

    public void I.callback() {
        Tester.event("callback");
    }

    declare parents: C implements I;

    after (C c) : target(c) && execution(public void C.run()) {
        Tester.event("after");
        ((I) c).trigger(); 
    }
    void around(I i)
        : target(i) 
        && execution(public void I.trigger()) 
        && if(i.test()) {
            Tester.event("around");
            i.callback();
        }
}
