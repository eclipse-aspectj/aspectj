import org.aspectj.lang.*;

public aspect PerCflow percflow(execution(* m(..))) {

  before(): execution(* Foo.*(..)) {}

  public static void main(String []argv) {
	print("before");
    new Foo().m();
    print("after");
  }
  
  public static void print(String prefix) {
    System.err.println(prefix);
    boolean b1 = Aspects14.hasAspect(PerCflow.class);
    boolean b2 = PerCflow.hasAspect();
    Object   o1 = (b1?Aspects14.aspectOf(PerCflow.class):null);
    Object   o2 = (b2?PerCflow.aspectOf():null);
    System.err.println("hasAspect?  "+b1+" : "+b2);
    System.err.println("aspectOf? "+o1+" : "+o2);   
  }
  
  public String toString() { return "PerCflowInstance"; }
}

class Foo {
  public void m() { PerCflow.print("during");}
}
