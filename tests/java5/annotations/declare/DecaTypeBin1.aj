
public aspect DecaTypeBin1 {
  declare @type: A : @Colored("red");
}

aspect X {
  before(): execution(* *(..)) && @this(Colored) {
    System.err.println("Color identified on "+this.getClass());
  }
}
