
public aspect AtMethod1 {
  declare @method: void m1(..) : @Colored("red");

}

aspect X {
  before(Colored c): call(* *(..)) && @annotation(c) {
    System.err.println("Colored method invocation at "+thisJoinPoint);
  }
}
