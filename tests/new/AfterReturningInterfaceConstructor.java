
import org.aspectj.testing.Tester;

/** @testcase PR#889 after returning advice on interface constructor */
public class AfterReturningInterfaceConstructor {
    public static void main (String[] args) {
        Tester.expectEvent("constructor");
        Tester.expectEvent("advice");
        new C();
        Tester.checkAllEvents();
    }     
}

interface I {}

class C implements I {
    C() {
        Tester.event("constructor");
    }
}

aspect A {
    int I.i;
    I.new() {
        i = 2;
    }
    after() returning: execution(I.new()) {
        Tester.event("advice");
    }
}
