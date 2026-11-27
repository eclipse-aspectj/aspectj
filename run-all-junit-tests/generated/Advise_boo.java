public aspect Advise_boo {
  before(): execution(* boo(..)) {
    System.out.println("boo running");
  }
}
