import org.aspectj.lang.annotation.*;

@Aspect
public class MyAspect {
  @Before("call(* *.*(..)) && target(x)")
  public void myAdvice(CharSequence x) {
    System.out.println();
  }
}
