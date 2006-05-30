import org.aspectj.lang.*;

public aspect PerThis perthis(execution(* m(..))) {

  before(): execution(* Foo.*(..)) {}

  public static void main(String []argv) {
	print("before");
    new Foo().m();
    print("after");
  }
  
  public static void print(String prefix) {
    System.err.println(prefix);
    boolean b1 = Aspects14.hasAspect(PerThis.class,null);
    boolean b2 = PerThis.hasAspect(null);
    Object   o1 = (b1?Aspects14.aspectOf(PerThis.class,null):null);
    Object   o2 = (b2?PerThis.aspectOf(null):null);
    System.err.println("hasAspect?  "+b1+" : "+b2);
    System.err.println("aspectOf? "+o1+" : "+o2);   
  }
  
  public String toString() { return "PerThisInstance"; }
}

class Foo {
  public void m() { print("during");}
  public void print(String prefix) {
    System.err.println(prefix);
    boolean b1 = Aspects14.hasAspect(PerThis.class,this);
    boolean b2 = PerThis.hasAspect(this);
    Object   o1 = (b1?Aspects14.aspectOf(PerThis.class,this):null);
    Object   o2 = (b2?PerThis.aspectOf(this):null);
    System.err.println("hasAspect?  "+b1+" : "+b2);
    System.err.println("aspectOf? "+o1+" : "+o2);   
  }
}
