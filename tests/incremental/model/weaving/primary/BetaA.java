package primary;

public aspect BetaA {

  pointcut m1call(): call(* m1(..));

  before(): m1call() {
    System.err.println("m1 got called");
  }
}
