public class SimpleEnum {
}

enum Foo {
}

class Bar {
}

interface I {
}

aspect X {
  before(): within(is(EnumType)) && staticinitialization(*) {}
}
