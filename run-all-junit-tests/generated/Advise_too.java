public aspect Advise_too {
  before(): execution(* too(..)) {
    System.out.println("too running");
  }
}
