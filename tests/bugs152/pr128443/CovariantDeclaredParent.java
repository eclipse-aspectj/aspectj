/**
 * Declaring class works - see Bug below for declaring interface.
 */
/*
public class CovariantDeclaredParent {
    interface Result {}

  public class Super {
      public Result result() {return null;}
  }
  class Sub extends Super {
      public C result() { return null; }
  }
  static aspect A {
      declare parents: C implements Result ; 
  }
  class C {}
}
*/


/**
 * Declaring interface on type should happen before any
 * covariant return types are evaluated.
 */
class Bug {
    interface Result {}
    interface Factory {
        Result getInstance();
    }   
    // uncomment to get more errors with default implementation
//  static aspect A  {
//    // default implementation
//    public Result Factory.getInstance() { 
//      throw new UnsupportedOperationException(); 
//    }
//  }

  // D is factory for B
  static aspect A_forB {
      // bug: this should work
      declare parents: B implements Result;
    // Bug: get error here wrt invalid return type
    public B D.getInstance() { 
      return new B(); 
    }
  }
  static class D implements Factory {}

  static class B   {}
  // to avoid the bug, declare interface directly on class
  // static class B  implements Result {}

}
