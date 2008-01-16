public aspect SimpleD {
  before(): execution(* foo(..)) {}

  public void foo() {}
}
