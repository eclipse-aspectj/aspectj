
import org.aspectj.testing.*;

/** PR#745 stack overflow expected when advice recurses into itself */
public class AdviceOnAdviceRecursion { // XXX n-aspect variant?
    public static void main (String[] args) {
        boolean passed = false;
        Throwable ex = null;
        try {
            C.m();
        } catch (StackOverflowError e) {
            passed = true;
        } catch (Throwable e) {
            ex = e;
        }
        Tester.check(passed, "expected StackOverflowError, got " + ex);
    }
}

class C {
    static void m() { ; }
}

aspect A {
    before() : within(C) || within(B) { 
        C.m();
    }
}

aspect B {
    before() : call(void m()) { }
}
