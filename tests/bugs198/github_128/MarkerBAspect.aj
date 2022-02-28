import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class MarkerBAspect {
  public static int proceedTimes = 1;

  @Around("@annotation(MarkerB) && execution(* *(..))")
  public Object intercept(ProceedingJoinPoint thisJoinPoint) throws Throwable {
    System.out.println("    >> Inner intercept");
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          for (int i = 0; i < proceedTimes; i++) {
            System.out.println("      >> Inner proceed");
            thisJoinPoint.proceed();
            System.out.println("      << Inner proceed");
          }
        }
        catch (Throwable throwable) {
          throwable.printStackTrace(System.out);
        }
      }
    }).start();
    System.out.println("    << Inner intercept");
    return null;
  }
}
