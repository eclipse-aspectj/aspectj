public class SimpleInner3 {
}

enum Foo {
}

@interface WibbleAnno {
}

class Bar {
  public class Inner {
    public void foo() {}
  }
  Runnable r = new Runnable() {
    public void run() {}
  };
}

interface I {
}

aspect X {
  before(): execution(* (is(InnerType) && !is(AnonymousType) && *).*(..)) {}
}
