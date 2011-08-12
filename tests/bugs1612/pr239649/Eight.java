public class Eight {
  public static void main(String[] argv) {
    Eight a = new Eight();
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
}

aspect X extends Y {
  pointcut p(): if(thisAspectInstance.doit());

  boolean doit() {
    System.out.println("in doit(): class="+this.getClass().getName());
    return true;
  }

}
