import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Colored { String color(); }

public class CallAnnBinding {
  public static void main(String[]argv) {
    new CallAnnBinding().m1();
    new CallAnnBinding().m2();
    new CallAnnBinding().m3();
  }

  @Colored(color="red")
  public void m1() {
    System.err.println("method1");
  }

  @Colored(color="green")
  public void m2() {
    System.err.println("method2");
  }

  @Colored(color="blue")
  public void m3() {
    System.err.println("method3");
  }

}

aspect X {
  int i = 0;
  
  before(Colored c): call(* *(..)) && !within(X) && @annotation(c) {
  	i++;
  	if (i==1 && !c.color().equals("red")) throw new RuntimeException("First time through should be red, but is "+c.color());
  	if (i==2 && !c.color().equals("green")) throw new RuntimeException("Second time through should be green, but is "+c.color());
  	if (i==3 && !c.color().equals("blue")) throw new RuntimeException("Third time through should be blue, but is "+c.color());
  	System.err.println(c.color());
  }
}

