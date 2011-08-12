public class Nine {
  public static void main(String[] argv) {
    Nine a = new Nine();
    a.m();
  }

  public void m() {
    System.out.println("Method m() running");
  }
}

abstract aspect Y {
  abstract pointcut p();
  before(): execution(* m(..))  && p() {
    System.out.println("In advice()");
  }
  boolean doit() {
    System.out.println("in doit(): class="+this.getClass().getName());
    return true;
  }
}

aspect X extends Y {
  pointcut p(): if(thisAspectInstance.doit());
}
