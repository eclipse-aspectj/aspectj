
import org.aspectj.testing.Tester; 

/** @testcase PR#535 */
public class PR535 {
    public static void main(String[] args) {
        Tester.expectEvent("In bar()");
        Tester.expectEvent("advice");
        Tester.expectEvent("In foo()");
        new C().foo();
        Tester.checkAllEvents();
    }
}

class C {

    public void foo() {
        Tester.event("In foo()");
        bar();
    }

    public void bar() {
        Tester.event("In bar()");
    }
}

aspect A {
   pointcut outside(): !cflow(within(A));

    void around(C c):
        cflow(execution(public void C.foo())) &&
        target(c) &&
        execution(public void C.bar()) &&
        outside() {
        Tester.event("advice");
        proceed(c);
    }
}
