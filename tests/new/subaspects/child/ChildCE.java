

package child;

import parent.ParentCE;

import org.aspectj.testing.*;

public class ChildCE implements I {
    public static void main (String[] args) {
        new Target().run();   
        Tester.checkAllEvents();
    } 
    static {
        Tester.expectEvent("define");
        Tester.expectEvent("run");
    }
}

interface I {
	public pointcut fromInterface(): call(* *(..));
}

class Target { 
    public void run(){
        Tester.event("run");
    }
}

/** @testcase PR#647 concrete aspect unable to access abstract package-private pointcut in parent for overriding */
aspect ParentChild extends ParentCE implements I {// expect CE here: child does not define "define()" b/c inaccessible
    protected pointcut define()  
        : call(public void Target.run());
}

