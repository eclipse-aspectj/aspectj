import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Colored { String color(); }

@Colored(color="yellow")
public class AtTarget1 {
  public static void main(String[]argv) {
    new AtTarget1().m();
  }

  @Colored(color="red")
  public void m() {
    System.err.println("method");
  }

}

aspect X {
  int adviceExecutions = 0;

  before(Colored c): call(* *(..)) && !within(X) && @target(c) {
    System.err.println(c.color());
    adviceExecutions++;
    
    if (!c.color().equals("yellow")) 
      throw new RuntimeException("Color should be yellow");
    
    if (adviceExecutions>1)
      throw new RuntimeException("Advice shouldn't be called more than once");
  }

}

