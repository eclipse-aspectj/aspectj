public class Twelve {
  public static void main(String[] argv) {
    Twelve a = new Twelve();
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

  before(String s):execution(* m(..))  && if(thisAspectInstance.doit()) && args(s) { 
    System.out.println("In advice() arg="+s+" tjpsp="+thisJoinPointStaticPart);
  }
}
