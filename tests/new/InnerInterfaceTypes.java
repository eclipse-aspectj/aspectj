import org.aspectj.testing.*;

import java.util.Vector;

/** @testcase PR#685 subaspect method declaration on superaspect inner interface (types) */
public class InnerInterfaceTypes {
    public static void main (String[] args) {
        Object o = new C().getThis();
        Tester.check(null != o, 
                     "null != new C().getThis()");
        ConcreteAspect cc = ConcreteAspect.aspectOf();
        Tester.check(null != cc, 
                     "null != ConcreteAspect.aspectOf()");
        Object p = cc.getField();
        Tester.check(null != p, "null != cc.getField()");
    } 
}
class C implements AbstractAspect.InnerInterface {}

abstract aspect AbstractAspect {    
    /** bug iff interface defined in abstract aspect 
     * - not outer or subaspect 
     */
    interface InnerInterface {}
    
    private Object privateField = new Object();

    Object getField() {
        return new Vector();  // bad CE: "type name not found"
    }
} 
