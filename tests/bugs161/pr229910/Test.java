public class Test {

  public static void main(String[] argv) {
    new Foo();
  }

}

interface I {}

abstract class AbstractFoo implements I {

  String f;

  AbstractFoo() {
  }
}

class Foo extends AbstractFoo {

  Foo() {
    super();
    f = "hello";
  }
}

aspect X {

 void around(): target(I) && set(* *) { proceed(); }

}
