
import org.aspectj.testing.*;

/** @testcase PR#685 subaspect method declaration on superaspect inner interface (names) */
public class InnerInterfaceNames {
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
    
    public Object publicField = new Object();

    Object getField() {
        return publicField; // bad CE "no instance available"
    }
} 

aspect ConcreteAspect extends AbstractAspect {
    /** bug iff method declaration on parent inner interface */
    public Object InnerInterface.getThis() {
        return this; // bug: no instance available
    } 
}


/*
abstract aspect Composition {    
    interface Component {}
    
    public Object publicField = new Object();

    Object test() {
        return publicField; // bad CE "no instance available"
    }
 
} 
aspect SampleComposition extends Composition {
      public Object Component.getThis() {
          return this;
      } 
}
*/


