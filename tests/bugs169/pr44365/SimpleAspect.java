public class SimpleAspect {
}

enum Foo {
}

@interface WibbleAnno {
}

class Bar {
  Runnable r = new Runnable() {
    public void run() {}
  };
}

interface I {
}

aspect X {
  before(): within(is(AspectType)) && staticinitialization(*) {}
}
