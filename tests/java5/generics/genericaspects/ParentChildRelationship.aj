import java.util.*;

public abstract aspect ParentChildRelationship<P,C> {
    
    /**
     * Parents contain a list of children
     */
    private List<C> P.children;
        
    /**
     * Each child has a parent
     */
    private P C.parent;

    /**
      * Parents provide access to their children
      */
    public List<C> P.getChildren() {
        return Collections.unmodifiableList(children);  
    }
    
    /**
     * A child provides access to its parent
     */
     public P C.getParent() {
       return parent;
     }
    
    /**
     * ensure bi-directional navigation on adding a child
     */
    public void P.addChild(C child) {
       if (child.parent != null) {
         child.parent.removeChild(child);
       }
       children.add(child);
       child.parent = this;
    }

    /**
     * ensure bi-directional navigation on removing a child
     */
    public void P.removeChild(C child) {
       if (children.remove(child)) {
         child.parent = null;
       }
    }

   /**
     * ensure bi-directional navigation on setting parent
     */
    public void C.setParent(P parent) {
       parent.addChild(this);
    }
    
    public pointcut addingChild(P p, C c) :
      execution(* P.addChild(C)) && this(p) && args(c);
      
    public pointcut removingChild(P p, C c) :
      execution(* P.removeChild(C)) && this(p) && args(c);
}
