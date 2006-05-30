import org.aspectj.lang.*;

public aspect PerTypeWithin pertypewithin(Foo) {

  before(): execution(* Foo.*(..)) {}

  public static void main(String []argv) {
	print("before");
    new Foo().m();
    print("after");
  }
  
  public static void print(String prefix) {
    System.err.println(prefix);
    boolean b1 = Aspects14.hasAspect(PerTypeWithin.class,Goo.class);
    boolean b2 = PerTypeWithin.hasAspect(Goo.class);
    Object   o1 = (b1?Aspects14.aspectOf(PerTypeWithin.class,Goo.class):null);
    Object   o2 = (b2?PerTypeWithin.aspectOf(Goo.class):null);
    System.err.println("hasAspect?  "+b1+" : "+b2);
    System.err.println("aspectOf? "+o1+" : "+o2);   
  }
  
  public String toString() { return "PerTypeWithinInstance"; }
}

class Goo {

}

class Foo {
  public void m() { print("during");}
  public void print(String prefix) {
    System.err.println(prefix);
    boolean b1 = Aspects14.hasAspect(PerTypeWithin.class,this.getClass());
    boolean b2 = PerTypeWithin.hasAspect(this.getClass());
    Object   o1 = (b1?Aspects14.aspectOf(PerTypeWithin.class,this.getClass()):null);
    Object   o2 = (b2?PerTypeWithin.aspectOf(this.getClass()):null);
    System.err.println("hasAspect?  "+b1+" : "+b2);
    System.err.println("aspectOf? "+o1+" : "+o2);   
  }
}
