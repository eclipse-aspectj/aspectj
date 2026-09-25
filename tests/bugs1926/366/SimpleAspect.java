import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.ProceedingJoinPoint;

@Aspect
public class SimpleAspect {
  private String message = "message";

  private void log(String s) {
    System.out.println(s);
  }

  @Around("execution(* Foo.bar(..))")
  public Object around(ProceedingJoinPoint pjp) throws Throwable {
    log(message);
    return pjp.proceed();
  }
}
