package test.aspects;

import test.aspects2.C2;


public class C3 {

    public void callAMethodC2() {
        C1 c1 = new C1();
        c1.aMethod(); // Should be a marker here...
        
        C2 c2 = new C2();
        c2.aMethod();  // Should be a marker here...
    }
    
    public void innerClassCall() {
        InnerClass ic = new InnerClass();
        
        ic.foo();
    }
    protected class InnerClass {
        public void foo() {
            C1 c1 = new C1();
            c1.aMethod();  // Should be a marker here...

            C2 c2 = new C2();
            c2.aMethod();  // Should be a marker here...
        }
    }
    
    public static void main(String [] args) {
        C1 c1 = new C1();
        
        c1.aMethod(); // Should be a marker here...
        c1.callAMethod();
        
        C3 c2 = new C3();
        
        c2.callAMethodC2();
        c2.innerClassCall();
    }
}
