import java.util.Locale;

aspect SwitchPatternPreview2Aspect {
  Object around(Object o): execution(* doSomethingWithObject(*)) && args(o) {
    System.out.println(
      switch (o) {
        case -1, 1 -> "special case:" + o;
        case Integer i && i > 0 -> "positive integer: " + o;
        case Integer i -> "other integer: " + o;
        default -> "non-integer: " + o;
      }
    );
    return proceed(o);
  }
}

class Application {
  public static void main(String[] args) {
    doSomethingWithObject(-1);
    doSomethingWithObject(0);
    doSomethingWithObject(42);
    doSomethingWithObject(-99);
    doSomethingWithObject("test");
    doSomethingWithObject(null);
  }

  public static Object doSomethingWithObject(Object o) { return o; }
}
