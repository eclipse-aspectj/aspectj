
public aspect AtCtor1 {
  declare @constructor: new(int) : @Colored("red");
}

aspect X {
  before(): call(new(..)) && @annotation(Colored) {
    System.err.println("Colored constructor invocation at "+thisJoinPoint);
  }
}
