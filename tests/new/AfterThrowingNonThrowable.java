
import org.aspectj.testing.Tester;
import java.util.*;

/** @testcase PR#832 after throwing advice with non-throwable formal */
public class AfterThrowingNonThrowable {

    public static void main(String[] args) {
        U.ee("after() throwing (Object o) : call(void C.run())");
        U.ee("run()");
        C c = new C();
        boolean gotError = false;
        try {
            c.run();
        } catch (Error e) {
            gotError = true;
        }
        Tester.check(gotError, "failed to get error");
        Tester.checkAllEvents();
    }
}

class C {
    public void run() {
        U.e("run()");
        throw new Error("");
    }
}

class U {
    static void e(String event) {
        //System.err.println(event);
        Tester.event(event);
    }
    static void ee(String event) {
        Tester.expectEvent(event);
    }
}

aspect A {
    after() throwing (Object o) : call(void C.run()) {
        U.e("after() throwing (Object o) : call(void C.run())");
    }
}
