import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class FirstAspect {
  @Around("execution(* doSomething())")
  public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
    System.out.println(getClass().getSimpleName());
    return joinPoint.proceed();
  }
}
