// HasMethod with anno value matching

@interface I {
  String s();
}
aspect A {
  declare parents: hasmethod(@I(s="abc") * *(..)) implements java.io.Serializable;
}

class B {
@I(s="abc")  public void m() {}
}
class C {
@I(s="def")  public void m() {}
}

