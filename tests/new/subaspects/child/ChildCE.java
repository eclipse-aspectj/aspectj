

package child;

import parent.ParentCE;

import org.aspectj.testing.*;

public class ChildCE {
    public static void main (String[] args) {
        new Target().run();   
        Tester.checkAllEvents();
    } 
    static {
        Tester.expectEvent("define");
        Tester.expectEvent("run");
    }
}

class Target { 
    public void run(){
        Tester.event("run");
    }
}

/** @testcase PR#647 concrete aspect unable to access abstract package-private pointcut in parent for overriding */
aspect ParentChild extends ParentCE {// expect CE here: child does not define "define()" b/c inaccessible
    protected pointcut define()  
        : call(public void Target.run());
}

