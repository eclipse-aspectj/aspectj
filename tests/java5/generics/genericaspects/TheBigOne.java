import java.util.*;
import org.aspectj.lang.annotation.*;

class Blob {}

public aspect TheBigOne extends ParentChildRelationship<Blob,Blob> {

  public static void main(String []argv) {
    Blob a = new Blob();
    Blob b = new Blob();
    Blob c = new Blob();
    Blob d = new Blob();
    Blob e = new Blob();

    // arrange as follows: A contains B,C,D and B contains E

    a.addChild(b);
    a.addChild(c);
    a.addChild(d);
    b.addChild(e);

    // now query the layout

    if (!e.getParent().equals(b)) 
      throw new RuntimeException("why is E not parent of B? "+e.getParent());
    if (!d.getParent().equals(a)) 
      throw new RuntimeException("why is A not parent of D? "+d.getParent());
    if (a.getChildren().size()!=3)
      throw new RuntimeException("A should have 3 children, not:"+a.getChildren().size());
  }

}


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
