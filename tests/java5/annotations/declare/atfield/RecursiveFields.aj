// check order of application - this should work
public aspect RecursiveFields {
  declare @field: public int * : @Colored("blue");
  declare @field: @Colored * * : @Fruit("orange");
}

aspect X {
  before(): set(@Fruit * *) {
    System.err.println("Fruit field access at "+thisJoinPoint);
  }
}
