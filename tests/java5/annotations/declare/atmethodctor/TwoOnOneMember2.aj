// trying to put two annotations onto one a method and two on a ctor
// - should be OK, they are diff annotations
public aspect TwoOnOneMember2 {
  declare @method: public void m1() : @Colored("red");
  declare @method: public void m1() : @Fruit("tomato");
  declare @constructor: new(int) : @Colored("green");
  declare @constructor: new(int) : @Fruit("apple");
}

aspect X {
  before(): call(* *(..)) && @annotation(Colored) {
    System.err.println("Colored method call at "+thisJoinPoint.getSourceLocation());
  }
  before(): call(* *(..)) && @annotation(Fruit) {
    System.err.println("Fruit method call at "+thisJoinPoint.getSourceLocation());
  }
  before(): call(new(..)) && @annotation(Colored) {
    System.err.println("Colored ctor call at "+thisJoinPoint.getSourceLocation());
  }
  before(): call(new(..)) && @annotation(Fruit) {
    System.err.println("Fruit ctor call at "+thisJoinPoint.getSourceLocation());
  }
}
