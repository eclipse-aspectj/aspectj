
import org.aspectj.testing.*;

/** @testcase PR#661 !target with second advice on casted call */
public class CallNotTarget {
    public static void main (String args []) {
        //new B().go();      // remove cast to avoid bug
        ((I) new B()).go(); 
        Tester.checkAllEvents();

        doit(new B());
        doit(new A());
    }
    static {
        Tester.expectEvent("A.before");
        Tester.expectEvent("A.before-not");
        Tester.expectEvent("Aspect.before-not");
        Tester.expectEvent("go");
    }

    static void doit(I i) {
        Tester.check(i != null, "null i");
        //System.out.println(i);
    }
}

interface I { public void go (); }

class A implements I {  
    public void go () { Tester.check(false, "A"); } 
}
class B implements I {
  public void go () { Tester.event("go"); }
}

aspect Aspect {
    
    pointcut pc() : call(void I.go()); // same result if pointcut not named

    before () : pc() { // remove this advice to avoid bug
        Tester.event("A.before");
    }
    before () : pc() && !target (A) { // change to !target(String) to avoid bug
        Tester.event("A.before-not");
    }
    before () : pc() && !target (Aspect) { // change to !target(String) to avoid bug
        Tester.event("Aspect.before-not");
    }

//     before(): call(void doit(I)) && !args(A) {
//         System.out.println("doit !A");
//     }
}
