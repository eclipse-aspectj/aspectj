

package child;

import parent.ParentMethodCE;

import org.aspectj.testing.*;

public class ChildMethodCE {
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

/** @testcase PR#647 attempt to concretize abstract aspect without access to abstract member method */ 
class ParentChild extends ParentMethodCE { // expect CE here: child does not define "defineMethod()" b/c inaccessible
    protected void defineMethod() {}
}

