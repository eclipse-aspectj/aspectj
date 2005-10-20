import java.util.*;
import org.aspectj.lang.annotation.*;

abstract aspect ParentChildRelationship<Parent,Child> {

  interface ParentHasChildren<C>{}
  interface ChildHasParent<P>{}

  declare parents: Parent implements ParentHasChildren<Child>;
  declare parents: Child  implements ChildHasParent<Parent>;

  public List<E> ParentHasChildren<E>.children = new ArrayList<E>();
  public P ChildHasParent<P>.parent;

  public List<D> ParentHasChildren<D>.getChildren() {
    return Collections.unmodifiableList(children);
  }

  public P ChildHasParent<P>.getParent() {
    return parent;
  }

  public void ChildHasParent<R>.setParent(R parent) {
    this.parent = parent;
    ParentHasChildren phc = (ParentHasChildren)parent;
    if (phc.getChildren().contains(this))
    phc.addChild(this);
  }

  public void ParentHasChildren<X>.addChild(X child) {
    if (((ChildHasParent)child).parent != null) {
      ((ParentHasChildren)((ChildHasParent)child).parent).removeChild(child);
    } else {
      ((ChildHasParent)child).setParent((ParentHasChildren)this);
    }
    children.add(child);
  }

  public void ParentHasChildren<Y>.removeChild(Y child) {
    if (children.remove(child)) {
      ((ChildHasParent)child).parent = null;
    }
  }

}
/*
abstract aspect ParentChildRelationship<Parent,Child> {

  // interface implemented by parents    
  interface ParentHasChildren<C>{
    List<C> getChildren();
    void addChild(C child);
    void removeChild(C child);
  }

  // interface implemented by children 
  interface ChildHasParent<P>{
    P getParent();
    void setParent(P parent);
  }

  // ensure the parent type implements ParentHasChildren<child type> 
  declare parents: Parent implements ParentHasChildren<Child>;

  // ensure the child type implements ChildHasParent<parent type>
  declare parents: Child  implements ChildHasParent<Parent>;

  // Inter-type declarations made on the *generic* interface types to provide 
  // default implementations.

  // list of children maintained by parent 
  public List<E> ParentHasChildren<E>.children = new ArrayList<E>();

  // reference to parent maintained by child 
  public P ChildHasParent<P>.parent;

  // Default implementation of getChildren for the generic
  //  type ParentHasChildren 
  public List<D> ParentHasChildren<D>.getChildren() {
    return Collections.unmodifiableList(children);  
  }

  // Default implementation of getParent for the generic
  //  type ChildHasParent 
  public P ChildHasParent<P>.getParent() {
    return parent;
  }

  // Default implementation of setParent for the generic type ChildHasParent.
  // Ensures that this child is added to the children of the parent too.
  public void ChildHasParent<R>.setParent(R parent) {
    ((ParentHasChildren)parent).addChild(this);
  }

  // Default implementation of addChild, ensures that parent of child is
  // also updated.
  public void ParentHasChildren<X>.addChild(X child) {
    if (((ChildHasParent)child).parent != null) {
      ((ParentHasChildren)((ChildHasParent)child).parent).removeChild(child);
    }
    children.add(child);
    ((ChildHasParent)child).parent = (ParentHasChildren)this;
  }

  // Default implementation of removeChild, ensures that parent of
  //  child is also updated.
  public void ParentHasChildren<Y>.removeChild(Y child) {
    if (children.remove(child)) {
      ((ChildHasParent)child).parent = null;
    }
  }

  // Matches at an addChild join point for the parent type P and child type C
    @SuppressAjWarnings
    public pointcut addingChild(Parent p, Child c) :
      execution(* Parent.addChild(Child)) && this(p) && args(c);
      
  // Matches at a removeChild join point for the parent type P and child type C
  @SuppressAjWarnings
  public pointcut removingChild(Parent p, Child c) :
    execution(* Parent.removeChild(Child)) && this(p) && args(c);

}
*/
