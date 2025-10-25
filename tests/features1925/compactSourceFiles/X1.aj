aspect X1 {
  before():execution(* main(..)) {
    System.out.println("before");
  }
}
