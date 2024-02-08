import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * https://github.com/eclipse-aspectj/aspectj/issues/279
 */
public class Application {
  public static AtomicInteger HELLO_COUNT = new AtomicInteger(0);
  public static AtomicInteger ASPECT_COUNT = new AtomicInteger(0);

  private static final int ROUNDS = 25;
  private static final int THREAD_COUNT = 2;
  private static final int TOTAL_COUNT = ROUNDS * THREAD_COUNT;
  private static final String CLASS_TO_LOAD = "GreeterImpl";

  public static void main(String[] args) throws Exception {
    for (int round = 0; round < ROUNDS; round++) {
      ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
      ParallelCapableClassLoader cl = new ParallelCapableClassLoader(Application.class.getClassLoader(), CLASS_TO_LOAD);
      for (int i = 0; i < THREAD_COUNT; i++) {
        executor.submit(() -> {
          try {
            Class<?> myClass = Class.forName(CLASS_TO_LOAD, true, cl);
            Greeter greeter = (Greeter) myClass.getConstructor(new Class<?>[] {}).newInstance();
            greeter.hello();
            HELLO_COUNT.incrementAndGet();
          }
          catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
      }
      executor.shutdown();
      executor.awaitTermination(60, TimeUnit.SECONDS);
    }

    assert HELLO_COUNT.get() == TOTAL_COUNT
      : String.format("Hello count should be %s, but is %s", TOTAL_COUNT, HELLO_COUNT.get());
    assert ASPECT_COUNT.get() == TOTAL_COUNT
      : String.format("Aspect count should be %s, but is %s", TOTAL_COUNT, ASPECT_COUNT.get());
  }
}
