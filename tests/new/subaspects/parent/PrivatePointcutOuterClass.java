
package parent;

import org.aspectj.testing.*;

/** PR#647 outer subaspects of an aspect with private pointcut */
public abstract aspect PrivatePointcutOuterClass {
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
    private pointcut definePrivate() 
        : execution(void PrivatePointcutOuterClass.main(..));
}
aspect InnerChild extends PrivatePointcutOuterClass { }

