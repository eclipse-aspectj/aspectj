
public aspect A4 {

  pointcut m1(): call(* *print*(..));
  
  after(): m1() {
    System.err.println("After call to print");
  }
}