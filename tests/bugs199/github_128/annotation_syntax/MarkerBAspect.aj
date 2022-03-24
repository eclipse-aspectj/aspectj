import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Aspect
public class MarkerBAspect {
  @Around("@annotation(MarkerB) && execution(* *(..))")
  public Object intercept(ProceedingJoinPoint thisJoinPoint) throws Throwable {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try {
          for (int i = 0; i < Application.proceedTimesInner; i++) {
            System.out.println("      >> Inner proceed");
            thisJoinPoint.proceed();
            System.out.println("      << Inner proceed");
          }
        }
        catch (Throwable throwable) {
          throwable.printStackTrace(System.out);
        }
      }
    };

    System.out.println("    >> Inner intercept");
    if (Application.useThreadPool)
      Application.executorService.submit(runnable);
    else
      new Thread(runnable).start();
    System.out.println("    << Inner intercept");
    return null;
  }
}
