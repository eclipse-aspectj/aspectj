
import org.aspectj.testing.Tester;

/** @testcase PR#827 after returning advice on interface and implementation constructor */
public class AfterReturningConstructor {
    public static void main(String[] args) {
        U.ee(U.before + U.ijp);
        U.ee(U.afterReturning + U.ijp);
        U.ee(U.before + U.cjp);
        U.ee(U.c);
        U.ee(U.afterReturning + U.cjp);
        U.ee("done");
        new C();
        U.e("done");
        Tester.checkAllEvents();
    }
}

class C implements I {
    C() { U.e("C()"); }
    public void run() { }
}

interface I {
    void run();
}

class U {
    static final String before = "before(): ";
    static final String after = "after(): ";
    static final String afterReturning = "after() returning(): ";
    static final String afterThrowing = "after() throwing(): ";
    static final String c = "C()";
    static final String i = "I()";
    static final String cjp = "initialization(" + c + ")";
    static final String ijp = "initialization(" + i + ")";

    static void e(String event) {
        //System.err.println("act event: " + event); // XXX
        Tester.event(event);
    }
    static void ee(String event) {
        //System.err.println("exp event: " + event); // XXX
        Tester.expectEvent(event);
    }
}

aspect A {
    /** must pick out both interface and implementor initializers */
    pointcut pc(): initialization(new(..)) && !within(A);

    before(): pc() {
        U.e(U.before + thisJoinPoint);
    }

    after() returning(): pc() {
        U.e(U.afterReturning + thisJoinPoint);
    }
}

