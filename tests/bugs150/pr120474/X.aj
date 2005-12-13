public aspect X {
  before(): execution(* foo(..)) && !within(X) { }
}
