public class SimpleClass {
}

interface Foo {
}

class Bar {
}

aspect X {
  before(): within(is(ClassType)) && staticinitialization(*) {}
}
