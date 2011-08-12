public class One {
  public static void main(String[] argv) {
    One a = new One();
    a.m();
  }

  public void m() {
    System.out.println("Method m() running");
  }
}

aspect X {
  boolean doit() {
    System.out.println("In instance check method doit()");
    return true;
  }

  before():execution(* m(..))  && if(thisAspectInstance.doit()){ 
    System.out.println("In advice()");
  }
}
