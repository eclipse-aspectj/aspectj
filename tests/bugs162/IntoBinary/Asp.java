public aspect Asp {
  before(): execution(* foo(..)) {}
}
