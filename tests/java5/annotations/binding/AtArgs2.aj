import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) @interface Colored   { String color(); }
@Retention(RetentionPolicy.RUNTIME) @interface Fruit     { String value(); }

@Colored(color="yellow") @Fruit("banana") class YB {}

public class AtArgs2 {
  public static void main(String[]argv) {
    m(new YB());
    if (!X.run) throw new Error("Advice should have run");
  }

  public static void m(Object a) {}

}

aspect X {
  public static boolean run = false;
  before(Colored c): call(* m(..)) && !within(X) && @args(c) {
    System.err.println("Color is "+c.color());
    run = true;
    if (!c.color().equals("yellow")) 
      throw new RuntimeException("Color should be yellow: "+c.color());
  }

}

