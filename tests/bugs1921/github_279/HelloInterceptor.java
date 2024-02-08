import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class HelloInterceptor {
  @Around("execution(public String Greeter.hello())")
  public Object interceptHello(ProceedingJoinPoint pjp) throws Throwable {
    Application.ASPECT_COUNT.incrementAndGet();
    return pjp.proceed();
  }
}
