public class Eleven {
  public static void main(String[] argv) {
    Eleven a = new Eleven();
    a.m();
  }

  public void m() {
    System.out.println("Method m() running");
  }
}

aspect X perthis(this(Eleven)) {

  boolean doit() {
    System.out.println("In instance check method doit()");
    return true;
  }

  before():execution(* m(..))  && if(thisAspectInstance.doit()){ 
    System.out.println("In advice()");
  }
}
