public class Fourteen {
  public static void main(String[] argv) {
    Fourteen a = new Fourteen();
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

  before(String s):args(s) && execution(* m(..))  && if(printit(thisAspectInstance)) && if(thisJoinPointStaticPart.toString().indexOf("Fourteen")!=-1) { 
    System.out.println("In advice() arg="+s+" tjpsp="+thisJoinPointStaticPart);
  }
}
