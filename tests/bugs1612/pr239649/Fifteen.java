public class Fifteen {
  public static void main(String[] argv) {
    Fifteen a = new Fifteen();
    a.m();
  }

  public void m() {
    System.out.println("Method m() running");
  }
}

abstract aspect Y {
  abstract pointcut p();
  before(): if(thisAspectInstance.doit()) && p() {
    System.out.println("In advice()");
  }
  boolean doit() {
    System.out.println("in doit(): class="+this.getClass().getName());
    return true;
  }
}

aspect X extends Y {
  pointcut p(): execution(* m(..));
}
