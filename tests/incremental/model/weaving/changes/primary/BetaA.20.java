package primary;

public aspect BetaA {

  pointcut m1call(): call(* m1(..));

  before(): m1call() {
    System.err.println("m1 got called");
  }

  pointcut m2call(): call(* m2(..));

  before(): m2call() {
    System.err.println("m2 got called");
  }
}
