import java.util.*;
import java.lang.reflect.*;
import org.aspectj.lang.annotation.*;

abstract aspect ParentChildRelationship<Parent,Child> {

  interface ParentHasChildren<C>{}
  interface ChildHasParent<P>{}

  declare parents: Parent implements ParentHasChildren<Child>;
  declare parents: Child  implements ChildHasParent<Parent>;

  public List<C> ParentHasChildren<C>.children;
  public P ChildHasParent<P>.parent;

}

aspect GenericAspectO extends ParentChildRelationship<Top,Bottom> { 


  public static void main(String []argv) {

    Top t = new Top();
    Bottom.parent = t; // error - its not a static field
    List<Bottom> kids = new ArrayList<Bottom>();
    kids.add(t);
    Top.children = kids; // error - its not a static field


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
   TestO promoted the fields up - a parent knows its children, a 
         child knows its parents - but then uses them incorrectly

public abstract aspect ParentChildRelationship<Parent,Child> {

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

