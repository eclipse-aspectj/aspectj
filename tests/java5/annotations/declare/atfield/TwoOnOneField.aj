// trying to put two annotations onto one field
public aspect TwoOnOneField {
  declare @field: public int * : @Colored("red");
  declare @field: public int * : @Colored("blue");
}

aspect X {
  before(): set(@Colored * *) {
    System.err.println("Colored field access at "+thisJoinPoint);
  }
}
