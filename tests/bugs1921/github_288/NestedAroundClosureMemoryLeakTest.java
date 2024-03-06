import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NestedAroundClosureMemoryLeakTest {

  public static void main(String[] args) throws Exception {
    if (args.length > 0 && "oom".equals(args[0]))
      testNoMemoryLeak_SystemShouldNotRunOutOfMemory();
    else
      testNoMemoryLeak_InheritableThreadLocalCleared();
  }

  /**
   * Tests that the inheritable thread-locals of the spawned threads are either null or contain all null elements
   */
  public static void testNoMemoryLeak_InheritableThreadLocalCleared() throws Exception {
    int numThreadPools = 1;
    List<ExecutorService> executorServices = createExecutorServicesWithFixedThreadPools(numThreadPools);
    try {
      executeTasksAndGC(executorServices);

      Field mapField = Thread.class.getDeclaredField("inheritableThreadLocals");
      mapField.setAccessible(true);
      Set<Thread> threads = Thread.getAllStackTraces().keySet();

      threads.stream()
        .filter(thread -> thread.getName().contains("pool"))
        .forEach(thread -> {
          try {
            Object inheritableThreadLocals = mapField.get(thread);
            if (inheritableThreadLocals != null) {
              Field tableField = inheritableThreadLocals.getClass().getDeclaredField("table");
              tableField.setAccessible(true);
              Object[] inheritableThreadLocalTable = (Object[]) tableField.get(inheritableThreadLocals);
              if (inheritableThreadLocalTable != null) {
                for (Object entry : inheritableThreadLocalTable)
                  assert entry == null : "All inheritable thread-locals should be null after GC";
              }
            }
          }
          catch (Exception e) {
            throw new RuntimeException(e);
          }
        });

      System.out.println("Test passed - all inheritable thread-locals are null after GC");
    }
    finally {
      for (ExecutorService executorService : executorServices)
        executorService.shutdown();
    }
  }

  /**
   * Executes tasks in multiple threads, using one executor service with a fixed thread pool of size 1 per task. This
   * ensures that each spawned thread gets initialised for the first time and allocates memory for inheritable
   * thread-locals, exposing possible memory leaks when running @AspectJ aspects with non-inlined, nested around advices
   * in multi-thread situations.
   * <p>
   * If each thread allocates memory for a stack of around closures (memory leak case) according to
   * <a href="https://github.com/eclipse-aspectj/aspectj/issues/288">GitHub issue 288</a>, the program will run out of
   * memory. When fixed, this should no longer happen.
   * <p>
   * Run this test e.g. with {@code -Xmx1G} for 10 x 128 MB memory consumption to ensure an out of memory error in the
   * leak case. Any other appropriate combination of number of threads and memory limit is also OK.
   */
  public static void testNoMemoryLeak_SystemShouldNotRunOutOfMemory() throws Exception {
    int numThreadPools = 5;
    List<ExecutorService> executorServices = createExecutorServicesWithFixedThreadPools(numThreadPools);
    try {
      executeTasksAndGC(executorServices);
      System.out.println("Test passed - no OutOfMemoryError due to inheritable thread-locals memory leak");
    }
    finally {
      for (ExecutorService executorService : executorServices)
        executorService.shutdown();
    }
  }

  private static List<ExecutorService> createExecutorServicesWithFixedThreadPools(int count) {
    List<ExecutorService> executorServiceList = new ArrayList<>(count);
    for (int i = 0; i < count; i++)
      executorServiceList.add(Executors.newFixedThreadPool(1));
    return executorServiceList;
  }

  private static void executeTasksAndGC(List<ExecutorService> executorServices) throws Exception {
    for (ExecutorService executorService : executorServices)
      new MemoryHog(executorService).doSomething();
    System.out.println("Finished executing tasks");

    // Best effort GC
    System.gc();
    System.out.println("Finished executing GC");

    // Sleep to take a memory dump
    // Thread.sleep(500000);
  }

}
