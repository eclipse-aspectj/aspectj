
public aspect A2 {

  pointcut m1(): execution(* main(..));
  
  after(): m1() {
    System.err.println("After main runs");
  }
}
