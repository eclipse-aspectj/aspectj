public class SimpleAnonymous {
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
  before(): within(is(AnonymousType)) && staticinitialization(*) {}
}
