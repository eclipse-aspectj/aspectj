import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) @interface Colored   { String color(); }
@Retention(RetentionPolicy.RUNTIME) @interface Fruit     { String value(); }

@Colored(color="yellow") @Fruit("banana") class YB {}

public class AtArgs3 {
  public static void main(String[]argv) {
    m(new YB());
    if (!X.b)
      throw new Error("Advice should have run");
  }

  public static void m(Object a) {}

}

aspect X {

  static boolean b = false;

  before(): call(* m(..)) && !within(X) && @args(Colored) {
    b=true;
  }

}

