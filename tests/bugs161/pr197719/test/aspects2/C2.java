package test.aspects2;

import test.aspects.C1;


public class C2 extends C1 {
    public void callAMethodC2() {
        aMethod(); // Should be a marker here...
    }
    
    public void innerClassCall() {
        InnerClass ic = new InnerClass();
        
        ic.foo();
    }
    protected class InnerClass {
        public void foo() {
            aMethod(); // Should be a marker here...
        }
    }
    
    public static void main(String [] args) {
        C1 c1 = new C1();
        
        c1.callAMethod();
        
        C2 c2 = new C2();
        
        c2.aMethod(); // Should be a marker here...
        c2.callAMethod();
        c2.callAMethodC2();
        c2.innerClassCall();
    }
}
