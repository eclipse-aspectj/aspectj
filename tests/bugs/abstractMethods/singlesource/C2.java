abstract class B {}
 
aspect A {
  public abstract void B.m();

  public static void doit(B b) { b.m(); }
}

public class C2 extends B {
  public static void main(String[] args) {
    A.doit(new C2());
  }
  
  public void m() {}
}

