aspect X {
  before(): staticinitialization(Fo*) { }
  before(): call(Fo*.new(..)) { }
}
