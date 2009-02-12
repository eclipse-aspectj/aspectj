package p;

public aspect X {
  before(): staticinitialization(!X) {}
}
