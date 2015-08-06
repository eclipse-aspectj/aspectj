// HasMethod with anno value matching

@interface I {
  Class i();
}
aspect A {
  declare parents: hasmethod(@I(i=String.class) * *(..)) implements java.io.Serializable;
}

public class B {
@I(i=String.class)  public void m() {}
  public static void main(String []argv) {
    B b = new B();
    if (!(b instanceof java.io.Serializable)) throw new IllegalStateException("");
  }
}
class C {
@I(i=Integer.class)  public void m() {}
}

