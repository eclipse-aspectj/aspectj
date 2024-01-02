import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class FirstAspect {
  @Before("execution(@MarkerOne * *(..))")
  public void beforeAdvice(JoinPoint joinPoint){
    System.out.println("FirstAspect: " + joinPoint);
  }
}
