import java.util.*;

public privileged aspect TestAspect {

  pointcut p(Test t):
    target(t) &&
    get(!public Set<Number+> *Set) &&
    !within(TestAspect);

  Set around(Test t):p(t) {
    Set s = proceed(t);
    return s;
  }

  public static void main(String []argv) {

    Set<Integer> si = new Test1().foo();
    Set<Double>  sd = new Test2().foo();
  }

}
