import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;

@Aspect("percflow(within(C))")
public class A {

  @Before("execution(* foo(..)) && cflow(execution(* bar(..)) && this(o))")
  public void m(Object o) {}

  public static void main(String[] argv) {
    new C().bar();
  }

}

class C {
  public void bar() {
    foo();
  }
  public void foo() {}
}
