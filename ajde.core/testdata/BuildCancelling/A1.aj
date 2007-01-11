
public aspect A1 {

  pointcut m1(): execution(* main(..));
  
  before(): m1() {
    System.err.println("Before main runs");
  }
}