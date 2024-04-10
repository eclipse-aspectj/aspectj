import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Task {
  final ExecutorService taskManager;

  public Task(final ExecutorService executorService) {
    taskManager = executorService;
  }

  public void doSomething() throws ExecutionException, InterruptedException {
    Future<?> future = taskManager.submit(Task::toIntercept);
    future.get();
  }

  public static void toIntercept() {
    //System.out.println("Executing task")
  }
}
