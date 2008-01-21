aspect X {
  before(): execution(* f*(..)) {
    System.out.println("advised");
  }
}
