package one;

import org.aspectj.testing.*;
public aspect TestAspect {
    public static void main(String[] args) {
        C me = new C();
        me.foo();
        Tester.checkAllEvents();
    }
    static {
        Tester.expectEvent("execution(void one.C.publicMethod())");
        Tester.expectEvent("execution(void one.C.protectedMethod())");
        Tester.expectEvent("execution(void one.C.defaultMethod())");
        Tester.expectEvent("get(int one.C.protectedInt)");
        Tester.expectEvent("get(int one.C.publicInt)");
        Tester.expectEvent("get(int one.C.defaultInt)");

        // XXX added - correct?
        Tester.expectEvent("execution(void one.C.foo())");
        Tester.expectEvent("execution(void one.C.protectedMethod())");
    }

    before () : execution(* C.*(..)) || get(int C.*) 
        {
        Tester.event("" + thisJoinPointStaticPart); 
        //System.out.println("" + thisJoinPointStaticPart); 
    }
}
