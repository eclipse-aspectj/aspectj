
import org.aspectj.testing.Tester;

/** @testcase PR#823 private abstract method declared on inner interface */
public class PrivateMethodOnInnerInterface {

    public static void main(String[] args) {
        new C();
        Tester.expectEvent("method");
        Tester.checkAllEvents();
        // XXX CF test that method is not visible here 
    }
}

class C { }

abstract aspect A {
    // no bug unless not member of the same aspect
    interface I {}                                       
    interface J extends I{}                                       
}
aspect B extends A {

    declare parents : C implements J;
    private abstract int I.privateMethod();   
    
    private int C.privateMethod() { 
        Tester.event("method");
        return 0;
    }
    after(I i) returning : target(i) && initialization(I.new()) {
        i.privateMethod();
    }
}       


