public aspect Azpect1 {
  before(): execution(* main(..)) {
    System.out.println("Azpect1.before running");
  }
}
