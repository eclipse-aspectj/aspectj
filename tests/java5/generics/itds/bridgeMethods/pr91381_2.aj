public aspect pr91381_2 {
  public abstract Integer A.foo();

  public static void main(String[] args) {
    A a = new B();
    System.out.println(a.foo());
  }
}

abstract class A {
//  abstract Integer foo();
}

class B extends A {
  public Object foo() { return new Integer(42); }
}
