abstract class B {}
 
aspect A {
  abstract void B.m();

  public static void doit(B b) { b.m(); }
}

public class C extends B {
  public static void main(String[] args) {
    A.doit(new C());
  }
}

