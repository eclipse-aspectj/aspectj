public class Four {
  public static void main(String[] argv) {
    Four a = new Four();
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
    System.out.println("In advice() "+thisJoinPointStaticPart);
  }
}
