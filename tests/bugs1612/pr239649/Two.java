public class Two {
  public static void main(String[] argv) {
    Two a = new Two();
    a.m();
    a.m();
    a.m();
    a.m();
  }

  public void m() {
    System.out.println("Method m() running");
  }
}

aspect X {
  int count = 0;

  boolean doit() {
    count++;
    System.out.println("In instance check method, count="+count+" so doit returns "+((count%2)==0));
    return (count%2)==0;
  }

  before():call(* m(..))  && if(thisAspectInstance.doit()){ 
    System.out.println("In advice()");
  }
}
