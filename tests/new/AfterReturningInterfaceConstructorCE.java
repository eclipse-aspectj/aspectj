
import org.aspectj.testing.Tester;

/** @testcase PR#889 after returning advice on interface constructor */
public class AfterReturningInterfaceConstructorCE {
    public static void main (String[] args) {
        Tester.expectEvent("constructor");
        Tester.expectEvent("advice");
        I i = new C();
        System.out.println("i.i: " + i.i);
        
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
        System.out.println("running I.new()");
    }
    after() returning: execution(I.new()) {    // ERR: can't define constructor on interface
        Tester.event("advice");
    }
}
