import java.util.*;
import java.lang.reflect.*;
import org.aspectj.lang.annotation.*;

abstract aspect ParentChildRelationship<Parent,Child> {

  interface ParentHasChildren<C>{}
  interface ChildHasParent<P>{}

  declare parents: Parent implements ParentHasChildren<Child>;
  declare parents: Child  implements ChildHasParent<Parent>;

}

aspect GenericAspectN extends ParentChildRelationship<Top,Bottom> { 


  public static void main(String []argv) {

    // Check the state of top
    Top t = new Top();
    check(t instanceof ParentHasChildren,"Top should implement ParentHasChildren");
    Type[] intfs = Top.class.getGenericInterfaces();
    check(intfs[0] instanceof ParameterizedType,
          "Expected Top to have parameterized interface but found "+intfs[0]);
    ParameterizedType pt = (ParameterizedType) intfs[0];
    Type[] tArgs = pt.getActualTypeArguments();
    check(tArgs[0]==Bottom.class,
          "Expecting Bottom parameter but found " + tArgs[0]);


    // Check the state of top
    Bottom b = new Bottom();
    check(b instanceof ChildHasParent,"Bottom should implement ChildHasParent");
    intfs = Bottom.class.getGenericInterfaces();
    check(intfs[0] instanceof ParameterizedType,
          "Expected Bottom to have parameterized interface but found "+intfs[0]);
    pt = (ParameterizedType) intfs[0];
    tArgs = pt.getActualTypeArguments();
    check(tArgs[0]==Top.class,
          "Expecting Top parameter but found " + tArgs[0]);

  }

  public static void check(boolean b,String msg) {
    if (!b) throw new RuntimeException(msg);
  }
}

class Top {}
class Bottom {}

//////////////////////////////////////////////////////////////////

/* End game for test Z, as bits work they are promoted up into the 
   testcase above :)

   TestN promoted the declare parents statements up

public abstract aspect ParentChildRelationship<Parent,Child> {

  // Inter-type declarations made on the *generic* interface types to provide 
  // default implementations.

  public List<C> ParentHasChildren<C>.children;

  public P ChildHasParent<P>.parent;

  public List<C> ParentHasChildren<C>.getChildren() {
        return Collections.unmodifiableList(children);  
  }

  public P ChildHasParent<P>.getParent() {
       return parent;
  }

  public void ParentHasChildren<C>.addChild(C child) {
       if (child.parent != null) {
         child.parent.removeChild(child);
       }
       children.add(child);
       child.parent = this;
    }

   public void ParentHasChildren<C>.removeChild(C child) {
       if (children.remove(child)) {
         child.parent = null;
       }
    }

    public void ChildHasParent<P>.setParent(P parent) {
       parent.addChild(this);
    }

    @SuppressAjWarnings
    public pointcut addingChild(Parent p, Child c) :
      execution(* Parent.addChild(Child)) && this(p) && args(c);
      
    @SuppressAjWarnings
    public pointcut removingChild(Parent p, Child c) :
      execution(* Parent.removeChild(Child)) && this(p) && args(c);
}
*/

