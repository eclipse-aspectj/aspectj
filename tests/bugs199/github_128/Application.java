import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Application {
  static int proceedTimesOuter;
  static int proceedTimesInner;
  static boolean useThreadPool = false;
  static ExecutorService executorService = Executors.newFixedThreadPool(2);

  @MarkerA
  @MarkerB
  public void doSomething() {
    System.out.println("        Doing something");
  }

  public static void main(String[] args) throws ExecutionException, InterruptedException {
    proceedTimesOuter = Integer.parseInt(args[0]);
    proceedTimesInner = Integer.parseInt(args[1]);
    useThreadPool = args.length > 2 && args[2].trim().equalsIgnoreCase("true");
    if (useThreadPool)
      prepopulateFixedThreadPool();

    new Application().doSomething();
    Thread.sleep(500);
  }

  private static void prepopulateFixedThreadPool() throws InterruptedException, ExecutionException {
    Future<?> future1 = executorService.submit(() -> {
      try { Thread.sleep(250); }
      catch (InterruptedException e) { e.printStackTrace(); }
    });
    Future<?> future2 = executorService.submit(() -> {
      try { Thread.sleep(250); }
      catch (InterruptedException e) { e.printStackTrace(); }
    });
    future1.get();
    future2.get();
  }

}
