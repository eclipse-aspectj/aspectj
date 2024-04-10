import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NestedAroundClosureMemoryLeakTest {

  private static final int NUM_THREAD_POOLS = 4;
  private static final int THREAD_POOL_SIZE = 3;

  public static void main(String[] args) throws Exception {
    testNoMemoryLeak_ThreadLocalCleared();
  }

  /**
   * Tests that the thread-locals of the spawned threads are either null or contain all null elements
   */
  public static void testNoMemoryLeak_ThreadLocalCleared() throws Exception {
    List<ExecutorService> executorServices = createExecutorServicesWithFixedThreadPools();
    try {
      executeTasks(executorServices);

      Field mapField = Thread.class.getDeclaredField("threadLocals");
      mapField.setAccessible(true);
      Set<Thread> threads = Thread.getAllStackTraces().keySet();
      System.out.println("Number of pool threads = " + threads.stream().filter(thread -> thread.getName().contains("pool")).count());

      threads.stream()
        .filter(thread -> thread.getName().contains("pool"))
        .forEach(thread -> {
          try {
            Object threadLocals = mapField.get(thread);
            if (threadLocals != null) {
              Field tableField = threadLocals.getClass().getDeclaredField("table");
              tableField.setAccessible(true);
              Object[] threadLocalTable = (Object[]) tableField.get(threadLocals);
              if (threadLocalTable != null) {
                for (Object entry : threadLocalTable) {
                  if (entry == null)
                    continue;
                  Field entryValueField = entry.getClass().getDeclaredField("value");
                  entryValueField.setAccessible(true);
                  throw new RuntimeException(
                    "All thread-locals should be null, but found entry with value " + entryValueField.get(entry)
                  );
                }
              }
            }
          }
          catch (RuntimeException rte) {
            throw rte;
          }
          catch (Exception e) {
            throw new RuntimeException(e);
          }
        });

      System.out.println("Test passed - all thread-locals are null");
    }
    finally {
      for (ExecutorService executorService : executorServices)
        executorService.shutdown();
    }
  }

  private static List<ExecutorService> createExecutorServicesWithFixedThreadPools() {
    List<ExecutorService> executorServiceList = new ArrayList<>(NestedAroundClosureMemoryLeakTest.NUM_THREAD_POOLS);
    for (int i = 0; i < NestedAroundClosureMemoryLeakTest.NUM_THREAD_POOLS; i++)
      executorServiceList.add(Executors.newFixedThreadPool(THREAD_POOL_SIZE));
    return executorServiceList;
  }

  private static void executeTasks(List<ExecutorService> executorServices) throws Exception {
    for (ExecutorService executorService : executorServices) {
      for (int i = 0; i < THREAD_POOL_SIZE * 2; i++)
        new Task(executorService).doSomething();
    }
    System.out.println("Finished executing tasks");

    // Sleep to take a memory dump
    // Thread.sleep(500000);
  }

}
