public aspect AspectAfterReturning {


  after() returning(int i): call(* ret*(..)) {
    System.err.println("Returning I="+i);
  }

  after() returning(Integer i): call(* ret*(..)) {
    System.err.println("Returning Integer="+i);
  }

  after() returning(Object i): call(* ret*(..)) {
    System.err.println("Returning Object="+i);
  }
  

  public static void main(String []argv) {
    retI();
    retInteger();
  }

  public static int retI() {
    return 5;
  }

  public static Integer retInteger() {
    return new Integer(10);
  }
   

}
