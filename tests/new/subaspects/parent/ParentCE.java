
package parent;

import org.aspectj.testing.*;

/** @testcase PR#647 concrete aspect unable to access abstract package-private pointcut in parent for overriding */
public abstract aspect ParentCE {
    abstract pointcut define();
    
    public abstract pointcut fromInterface();
    
    pointcut withSig(int i): args(i);
    
    before() : define() {
        Tester.event("define");
    }
}
aspect Child extends ParentCE {
    pointcut define() : call(public void Runnable.run());
    public pointcut fromInterface(): call(* *(..));
    
    pointcut withSig(): args();    // should be CE incompatible params
}
