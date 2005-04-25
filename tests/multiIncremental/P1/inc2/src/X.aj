public aspect X {
  before(): call(* *(..)) && !within(X) {
  }
}
