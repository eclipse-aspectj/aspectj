
package parent;

import org.aspectj.testing.*;

/** @testcase PR#647 inner subaspects of an aspect with private pointcut */
public abstract aspect PrivatePointcut {
    public static void main (String[] args) {
        Tester.event("main");
        Tester.checkAllEvents();
    } 
    static {
        Tester.expectEvent("main");
        Tester.expectEvent("definePrivate");
    }
    
    before() : definePrivate() {
        Tester.event("definePrivate");
    }

    /** private must be implemented in defining class */
    private pointcut definePrivate() : execution(void PrivatePointcut.main(..));
}
aspect InnerChild extends PrivatePointcut {
}

