import org.aspectj.lang.annotation.*;

@RequiredTypes("A")
@Aspect
class X {
  @Before("execution(* Code.*(..))")
  public void m() {
    System.out.println("x");
  }
}
