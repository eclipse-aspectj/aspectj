aspect X {
  before(): execution(* run(..)) {
    System.out.println("advice runnning");
  }
}
