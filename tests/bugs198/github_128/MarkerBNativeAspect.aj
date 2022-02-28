import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public aspect MarkerBNativeAspect {
  Object around() : @annotation(MarkerB) && execution(* *(..)) {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try {
          for (int i = 0; i < Application.proceedTimesInner; i++) {
            System.out.println("      >> Inner proceed");
            proceed();
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
