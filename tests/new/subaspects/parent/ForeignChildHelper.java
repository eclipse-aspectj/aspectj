
package parent;

import child.ForeignChildAspect;
import parent.SubAspectVisibility;

import org.aspectj.testing.*;

/** @testcase PR#647 inner, outer, and outside-package subaspects of an aspect with abstract protected-, public-, and default-access pointcuts */
public abstract aspect ForeignChildHelper extends SubAspectVisibility {
    /** @testCase override package-private pointcut in outer class */
    pointcut definePackagePrivate() : execution(void ForeignChildAspect.main(..));
    before() : definePackagePrivate() {
        Tester.event("ForeignChildHelper.definePackagePrivate");
    }
}
