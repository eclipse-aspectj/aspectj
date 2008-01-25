aspect SimpleAspect {
  before(): execution(* *(@Anno1 *)) {}
  before(): execution(* *(@Anno2 *)) {}
  before(): execution(* *(@Anno1 (*))) {}
  before(): execution(* *(@Anno2 (*))) {}
  before(): execution(* *(@Anno1 (@Anno2 *))) {}
  before(): execution(* *(@Anno2 (@Anno1 *))) {}
}
