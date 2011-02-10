aspect Foo {
 public <T extends I> void C.mitd(T something) {}
}

class C {
  <T extends I> void m(T something) {}
}

interface I {}

