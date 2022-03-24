aspect SwitchPatternPreview2Aspect {
  Object around(Integer i): execution(* doSomethingWithInteger(*)) && args(i) {
    System.out.println(
      switch (i) {
        case null -> "value unavailable: " + i;
        case -1, 1 -> "absolute value 1: " + i;
        case Integer value && value > 0 -> "positive integer: " + i;
        default -> "other integer: " + i;
      }
    );
    return proceed(i);
  }
}

class Application {
  public static void main(String[] args) {
    doSomethingWithInteger(-1);
    doSomethingWithInteger(0);
    doSomethingWithInteger(42);
    doSomethingWithInteger(-99);
    doSomethingWithInteger(Integer.valueOf(123));
    doSomethingWithInteger(null);
  }

  public static Object doSomethingWithInteger(Integer o) { return o; }
}
