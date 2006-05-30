import org.aspectj.lang.*;

public aspect Singleton {

  before(): execution(* Foo.*(..)) {}

  public static void main(String []argv) {
	print();
    new Foo().m();
    print();
  }
  
  public static void print() {
    boolean b1 = Aspects14.hasAspect(Singleton.class);
    boolean b2 = Singleton.hasAspect();
    Object   o1 = (b1?Aspects14.aspectOf(Singleton.class):null);
    Object   o2 = (b2?Singleton.aspectOf():null);
    System.err.println("hasAspect?  "+b1+" : "+b2);
    System.err.println("aspectOf? "+o1+" : "+o2);   
  }
  
  public String toString() { return "SingletonInstance"; }
}

class Foo {
  public void m() { }
}
