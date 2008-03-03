// HasMethod with anno value matching

@interface I {
  boolean b();
}
aspect A {
  declare parents: hasmethod(@I(b=true) * *(..)) implements java.io.Serializable;
}

class B {
@I(b=true)  public void m() {}
}
class C {
@I(b=false)  public void m() {}
}

