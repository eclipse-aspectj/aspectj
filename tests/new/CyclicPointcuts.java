
import org.aspectj.testing.Tester; 



/** PR#568 cyclic pointcut definitions */
public class CyclicPointcuts {
    
    static aspect A {  
        /* @testcase pointcut depends on itself */
        pointcut p(): p();              // CE 11 recursion not permitted
  
        /* @testcase two pointcuts depends on each other */  
        pointcut p1(): p2();            // CE 14  recursion not permitted
        pointcut p2(): p1();            // line 15
  
        /* @testcase three pointcuts depends on each other */  
        pointcut pa(): pb();            // CE 18  recursion not permitted
        pointcut pb(): pc();            // line 19
        pointcut pc(): pa();            // line 20
    }
}

/** @testcase three pointcuts in different classes in a cycle */  
aspect One {
    pointcut p() : Two.p();             // line 26
}
aspect Two {
    pointcut p() : Three.p();           // line 29
}
aspect Three {
    pointcut p() : One.p();             // CE 32  recursion not permitted
}

/** @testcase three pointcuts in different classes (sub, super, other) in a cycle */
abstract aspect Base {
    pointcut base() : A.p();            // line 37
}
aspect A {
    pointcut p() : Derived.p();         // line 40
}
aspect Derived extends Base {
    pointcut p() : base();                // CE 43  recursion not permitted
}

aspect Driver {
    // error test, but...
    static {
        Tester.expectEvent("Derived.p()");
        Tester.expectEvent("Three.p()");
        Tester.expectEvent("CyclicPointcuts.A.p()");
        Tester.expectEvent("CyclicPointcuts.A.p1()");
        Tester.expectEvent("CyclicPointcuts.A.()");
    }
    before() : Derived.p() { 
        Tester.event("Derived.p()");
    }
    before() : Three.p() { 
        Tester.event("Three.p()");
    }
    before() : CyclicPointcuts.A.p() { 
        Tester.event("CyclicPointcuts.A.p()");
    }
    before() : CyclicPointcuts.A.p1() { 
        Tester.event("CyclicPointcuts.A.p1()");
    }
    before() : CyclicPointcuts.A.pa() { 
        Tester.event("CyclicPointcuts.A.pa()");
    }
    
}


