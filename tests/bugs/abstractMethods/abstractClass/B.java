abstract class B {}
 
aspect A {
  abstract void B.m();

  public static void doit(B b) { b.m(); }
}