import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class SecondAspect {
  @Around("execution(@MarkerTwo * *(..))")
  public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
    System.out.println("SecondAspect: " + joinPoint);
    return joinPoint.proceed();
  }
}
