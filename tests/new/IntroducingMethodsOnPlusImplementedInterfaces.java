import org.aspectj.testing.Tester; 
public class IntroducingMethodsOnPlusImplementedInterfaces {
    public static void main(String[] args) {
        new IntroducingMethodsOnPlusImplementedInterfaces().realMain(args);
    }
    public void realMain(String[] args) {
        new D().f();
    }
    static {
        Tester.expectEvent("D.f");
    }
}

class D extends Thread {}
//static
aspect A {
    static interface I {}
    //(subtypes(Thread)) +implements I;
    //declare parents: (subtypes(Thread)) implements I;
    declare parents: Thread+ implements I;
    public void I.f() { Tester.event("D.f"); }
}
