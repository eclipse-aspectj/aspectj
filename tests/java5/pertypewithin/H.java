public aspect H pertypewithin(G) {


  after(): call(* *(..)) {
    System.err.println("advice running");
  }
}
