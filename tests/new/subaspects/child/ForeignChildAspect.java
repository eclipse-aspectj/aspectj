
package child;

import parent.SubAspectVisibility;
import parent.ForeignChildHelper;

import org.aspectj.testing.*;

/** @testcase PR#647 inner, outer, and outside-package subaspects of an aspect with abstract protected-, public-, and default-access pointcuts */
public aspect ForeignChildAspect extends ForeignChildHelper {
    public static void main (String[] args) {
        Tester.event("ForeignChildAspect.main");
    } 
    
    before() : defineProtected() {
        Tester.event("ForeignChildAspect.defineProtected");
    }

    before() : definePublic() {
        Tester.event("ForeignChildAspect.definePublic");
    }

    /** can be implemented */
    public pointcut definePublic() : execution(void ForeignChildAspect.main(..));

    /** can be implemented */
    protected pointcut defineProtected() : execution(void ForeignChildAspect.main(..));

}
