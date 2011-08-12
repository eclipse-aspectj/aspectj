public class Thirteen {
  public static void main(String[] argv) {
    Thirteen a = new Thirteen();
    a.m("abc");
  }

  public void m(String s) {
    System.out.println("Method m() running");
  }
}

aspect X {

  static boolean printit(Object o) {
    System.out.println("instance is "+o.getClass().getName());
return true;
  }

  before(String s):args(s) && execution(* m(..))  && if(printit(thisAspectInstance)){ 
    System.out.println("In advice() arg="+s+" tjpsp="+thisJoinPointStaticPart);
  }
}
