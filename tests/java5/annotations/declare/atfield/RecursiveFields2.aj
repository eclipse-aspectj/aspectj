// check order of application - this should work
public aspect RecursiveFields2 {
  declare @field: @Colored * * : @Fruit("orange");
  declare @field: public int * : @Colored("blue");
}

aspect X {
  before(): set(@Fruit * *) {
    System.err.println("Fruit field access at "+thisJoinPoint);
  }
}
