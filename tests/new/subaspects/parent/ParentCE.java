
package parent;

import org.aspectj.testing.*;

/** @testcase PR#647 concrete aspect unable to access abstract package-private pointcut in parent for overriding */
public abstract aspect ParentCE {
    abstract pointcut define();
    before() : define() {
        Tester.event("define");
    }
}
aspect Child extends ParentCE {
    pointcut define() : call(public void Target.run());
}
