
public aspect A3 {

  pointcut m1(): call(* *print*(..));
  
  before(): m1() {
    System.err.println("Calling print");
  }
}