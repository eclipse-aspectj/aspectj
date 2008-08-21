import java.util.*;

public class A {
  public void foo(Set s) {
  }
  public void goo(Map s) {
  }
}

aspect X {

  pointcut p(Set s): execution(* *(..)) && args(s);

  before(Map m): p(m) {
  }

}

