
/** @testcase PR#567 no error for cyclic class inheritance if no classes implement the interfaces */
public class CyclicClassInheritance {
    
  public static void main( String args[] ) {
      throw new Error("not to be run - error case ");
  }
}


class A extends B { A(); void a(){} }
class B extends A { B(); void b(){} }
