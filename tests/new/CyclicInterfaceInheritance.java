
/** @testcase PR#567 no error for cyclic interface inheritance if no classes implement the interfaces */
public class CyclicInterfaceInheritance {
    
  public static void main( String args[] ) {
      throw new Error("not to be run - error case ");
  }
}
// no bug if any class implements the interfaces
class C  {
    void a() { }
    void b() { }
}
interface A extends B { void a(); }  //ERR: A <- B <- A
interface B extends A { void b(); }  //ERR: B <- A <- B
