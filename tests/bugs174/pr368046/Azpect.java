public aspect Azpect {
  before(): execution(* *(..)) {
    System.out.println("advice");
  }
}
