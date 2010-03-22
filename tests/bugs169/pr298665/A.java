import java.util.*;

public class A<? extends M> {
  public void foo(List<?> m) {}
}
class B extends A {
  public void foo(List<? extends Object> m) {}
}
