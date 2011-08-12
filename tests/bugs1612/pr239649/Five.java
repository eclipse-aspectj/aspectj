public class Five {
  public static void main(String[] argv) {
    Five a = new Five();
    a.m("abc");
  }

  public void m(String s) {
    System.out.println("Method m() running");
  }
}

aspect X {
  boolean doit() {
    System.out.println("In instance check method doit()");
    return true;
  }

  before():execution(* m(..))  && if(thisAspectInstance.doit()){ 
    System.out.println("In advice() arg0="+thisJoinPoint.getArgs()[0]);
  }
}
