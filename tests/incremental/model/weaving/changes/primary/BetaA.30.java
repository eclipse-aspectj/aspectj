package primary;

public aspect BetaA {

  pointcut m2call(): call(* m2(..));

  before(): m2call() {
    System.err.println("m2 got called");
  }
}
