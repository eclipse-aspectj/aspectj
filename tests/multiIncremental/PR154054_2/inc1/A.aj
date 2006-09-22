public aspect A {
  before(): execution(* *(..)) { System.out.println("abc");}
  void around(): execution(* *(..)) { proceed();}

  void around(): execution(* *(..)) {
    try {
      proceed();
    } catch (Throwable e) {
    }
  }
}
