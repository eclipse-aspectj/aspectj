//Simple declare annotation attached to a field
public aspect AtField1 {
 declare @field: public int * : @Colored("red");
 declare parents: AtField1 implements java.io.Serializable;
}

aspect X {
  before(): set(@Colored * *) {
    System.err.println("Colored field access at "+thisJoinPoint);
  }
}
