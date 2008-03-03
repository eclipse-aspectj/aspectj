// HasMethod with anno value matching

@interface I {
  int i();
}
aspect A {
  declare parents: hasmethod(@I(i=5) * *(..)) implements java.io.Serializable;
}

class B {
@I(i=5)  public void m() {}
}
class C {
@I(i=6)  public void m() {}
}

