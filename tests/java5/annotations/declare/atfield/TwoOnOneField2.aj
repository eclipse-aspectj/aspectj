// trying to put two annotations onto one field - should be OK, they are diff annotations
public aspect TwoOnOneField2 {
  declare @field: public int * : @Colored("yellow");
  declare @field: public int * : @Fruit("banana");
}

aspect X {
  before(): set(@Colored * *) {
    System.err.println("Colored field access at "+thisJoinPoint);
  }
  before(): set(@Fruit * *) {
    System.err.println("Fruit field access at "+thisJoinPoint);
  }
}
