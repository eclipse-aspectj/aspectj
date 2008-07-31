public aspect X {
  before():execution(* *(..)) && !within(X) {}
}
