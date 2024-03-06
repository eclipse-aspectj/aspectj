import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class MemoryHog {
  final ExecutorService taskManager;
  // Use 128 MB of data, then run with -Xmx1G for 10 threads or -Xmx512M for 5 threads
  final byte[] someBigData = new byte[1024 * 1024 * 128];

  public MemoryHog(final ExecutorService executorService) {
    taskManager = executorService;
  }

  public void doSomething() throws ExecutionException, InterruptedException {
    Future<?> future = taskManager.submit(() -> System.out.println("Executing task"));
    future.get();
  }
}
