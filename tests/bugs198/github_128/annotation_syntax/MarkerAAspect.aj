import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclarePrecedence;

@Aspect
@DeclarePrecedence("MarkerAAspect, MarkerBAspect")
public class MarkerAAspect {
  @Around("@annotation(MarkerA) && execution(* *(..))")
  public Object intercept(ProceedingJoinPoint thisJoinPoint) throws Throwable {
    System.out.println(">> Outer intercept");
    Object result = null;
    for (int i = 0; i < Application.proceedTimesOuter; i++) {
      System.out.println("  >> Outer proceed");
      result = thisJoinPoint.proceed();
      System.out.println("  << Outer proceed");
    }
    System.out.println("<< Outer intercept");
    return result;
  }
}
