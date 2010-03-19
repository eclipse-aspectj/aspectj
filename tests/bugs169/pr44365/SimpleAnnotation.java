public class SimpleAnnotation {
}

enum Foo {
}

@interface WibbleAnno {
}

class Bar {
}

interface I {
}

aspect X {
  before(): within(is(AnnotationType)) && staticinitialization(*) {}
}
