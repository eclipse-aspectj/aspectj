
package parent;

import org.aspectj.testing.*;

/** @testcase PR#647 concrete aspect unable to access abstract package-private method in parent for overriding */
public abstract class ParentMethodCE {
    /** cannot be implemented outside this class */
    abstract void defineMethod();
}
