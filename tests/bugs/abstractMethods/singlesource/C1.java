abstract class B {}
 
aspect A {
  abstract void B.m();

  public static void doit(B b) { b.m(); }
}

public class C1 extends B {
  public static void main(String[] args) {
    A.doit(new C1());
  }
}

aspect A1 {
    void C1.m() {}
}
