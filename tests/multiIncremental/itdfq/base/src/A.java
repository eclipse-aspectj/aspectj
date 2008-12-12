package a.b.c;

public class A {
}

aspect X {
  B A.foo(C c) { return null; }
  declare parents: A implements java.io.Serializable;
}

class B {}

class C {}

aspect XX {
  public B.new(String s) {}
}
