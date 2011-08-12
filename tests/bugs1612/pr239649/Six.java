public class Six {
  public static void main(String[] argv) {
    Six a = new Six();
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

  before():call(* m(..))  && if(thisAspectInstance.doit()){ 
    System.out.println("In advice() "+thisEnclosingJoinPointStaticPart);
  }
}
