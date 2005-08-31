class Outer {
  public class Inner {}
}


public class pr104024 {
  public void varargs(Object... varargs) {}

  public void test() {
    Outer.Inner inner = new Outer().new Inner();
    varargs(inner); // works
    varargs(new Outer().new Inner()); // crashes
  }
}