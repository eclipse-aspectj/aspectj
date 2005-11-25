import java.util.*;
import java.lang.reflect.*;
import org.aspectj.lang.annotation.*;

          /** 
           * a generic aspect, we've used descriptive role names for the type variables
           * (Parent and Child) but you could use anything of course
           */
          /*public */ abstract aspect ParentChildRelationship<Parent,Child> {

            /** generic interface implemented by parents */   
            interface ParentHasChildren<C extends ChildHasParent>{
              List<C> getChildren();
              void addChild(C child);
              void removeChild(C child);
            }

            /** generic interface implemented by children */
            interface ChildHasParent<P extends ParentHasChildren>{
              P getParent();
              void setParent(P parent);
            }
          
            /** ensure the parent type implements ParentHasChildren<child type> */
            declare parents: Parent implements ParentHasChildren<Child>;
          
            /** ensure the child type implements ChildHasParent<parent type> */
            declare parents: Child implements ChildHasParent<Parent>;
          
            // Inter-type declarations made on the *generic* interface types to provide 
            // default implementations.
          
            /** list of children maintained by parent */
            public List<C> ParentHasChildren<C>.children = new ArrayList<C>();
          
            /** reference to parent maintained by child */        
            public P ChildHasParent<P>.parent;
          
            /** Default implementation of getChildren for the generic type ParentHasChildren */
            public List<C> ParentHasChildren<C>.getChildren() {
                  return Collections.unmodifiableList(children);  
            }
          
            /** Default implementation of getParent for the generic type ChildHasParent */    
            public P ChildHasParent<P>.getParent() {
                 return parent;
            }
          
            /** 
              * Default implementation of addChild, ensures that parent of child is
              * also updated.
              */ 
            public void ParentHasChildren<C>.addChild(C child) {
                 if (child.parent != null) {
                   child.parent.removeChild(child);
                 }
                 children.add(child);
                 child.parent = this;
              }
          
             /**
               * Default implementation of removeChild, ensures that parent of
               * child is also updated.
               */
             public void ParentHasChildren<C>.removeChild(C child) {
                 if (children.remove(child)) {
                   child.parent = null;
                 }
              }
          
              /**
                * Default implementation of setParent for the generic type ChildHasParent.
                * Ensures that this child is added to the children of the parent too.
                */
              public void ChildHasParent<P>.setParent(P parent) {
                 parent.addChild(this);
              }
                        
              /**
               * Matches at an addChild join point for the parent type P and child type C
               */    
             public pointcut addingChild(Parent p, Child c) :
               execution(* ParentHasChildren.addChild(ChildHasParent)) && this(p) && args(c);
               
              /**
                * Matches at a removeChild join point for the parent type P and child type C
                */    
              public pointcut removingChild(Parent p, Child c) :
                execution(* ParentHasChildren.removeChild(ChildHasParent)) && this(p) && args(c);

          }
          
aspect GenericAspectZ extends ParentChildRelationship<Top,Bottom> { 
	
    before(Top p,Bottom c): addingChild(p,c) {}
    before(Top p,Bottom c): removingChild(p,c) {}

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




    // Field fiddling
    b.parent = t;
    List<Bottom> kids = new ArrayList<Bottom>();
    kids.add(b);
    t.children = kids;


    // start using the methods
    List<Bottom> kids2 = t.getChildren();
    check(kids2.size()==1,
      "Expected one child of the Top but found "+kids2.size());
    check(kids2.get(0).equals(b),
      "Expected one child of the Top which was what we put in there!"+kids2.get(0));

    // and the parent methods
    Top retrievedParent = b.getParent();
    check(retrievedParent==t,
      "parent check 1 failed "+
      "retrieved="+retrievedParent+"  expected="+t);


    Top top2 = new Top();
    b.setParent(top2);
    Top retrievedParent2 = b.getParent();
    check(retrievedParent2==top2,
      "parent check 2 failed "+
      "retrieved="+retrievedParent2+"  expected="+top2);
    
    Top top3 = new Top();
    Bottom bot2 = new Bottom();
    top3.addChild(bot2);
    Bottom aBottom = top3.getChildren().get(0);
    check(aBottom==bot2,"Incorrect child? expected="+bot2+" found="+aBottom);
    top3.removeChild(bot2);
    int size=top3.getChildren().size();
    check(size==0,"Should be no children but there were "+size);


  }

  public static void check(boolean b,String msg) {
    if (!b) throw new RuntimeException(msg);
  }
}

class Top {}
class Bottom {}

