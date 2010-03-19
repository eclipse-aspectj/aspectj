public class SimpleInner2 {
}

enum Foo {
}

@interface WibbleAnno {
}

class Bar {
  public class Inner {}
  Runnable r = new Runnable() {
    public void run() {}
  };
}

interface I {
}

aspect X {
  before(): within(is(InnerType) && !is(AnonymousType)) && staticinitialization(*) {}
}
