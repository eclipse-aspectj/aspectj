import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Colored { String color(); }

@Colored(color="yellow")
public class AtThis1 {
  public static void main(String[]argv) {
    new AtThis1().m();
    X.verify();
  }

  @Colored(color="red")
  public void m() {
    System.err.println("method");
  }

}

aspect X {
  static int count = 0;

  before(Colored c): call(* *(..)) && !within(X) && @this(c) {
    System.err.println(thisJoinPoint+" > "+c.color());
    count++;
    
    if (!c.color().equals("yellow")) 
      throw new RuntimeException("Color should be yellow");
  }

  public static void verify() {
    if (count!=1) throw new Error("Should be 1 run: "+count);
  }

}

