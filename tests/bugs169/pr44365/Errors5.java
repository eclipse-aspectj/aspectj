public aspect Errors5 {
  before(): execution(* (is(InnerType) && *).*(..)) {}
  before(): execution(* (is)) && *).*(..)) {}
}

class C {
  class Foo {
    public void m() {}
  }
    public void m() {}
}
