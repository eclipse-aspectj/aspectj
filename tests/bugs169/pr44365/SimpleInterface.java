public class SimpleInterface {
}

interface Foo {
}

class Bar {
}

aspect X {
  before(): within(is(InterfaceType)) && staticinitialization(*) {}
}
