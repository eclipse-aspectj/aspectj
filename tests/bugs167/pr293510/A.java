
@Foo
class A {
  public void m() {}
}

aspect X {
//before(): execution(* (@Foo *..*).*(..)) {}
before(): execution(* (@Foo *).*(..)) {}
}

@interface Foo {}
