import java.lang.invoke.ConstantBootstraps;
import java.util.concurrent.Callable;

public class Application {
  public static void main(String[] args) throws Exception {
    Callable<?> first = new CondyCallable();
    Callable<?> second = new CondyCallable();
    if (!(first.call() == second.call()))
      throw new RuntimeException("Non-identical ConstantDynamic values (should never happen)");
  }

  /**
   * Class {@link CondyCallable} dispatches to this constructor via {@link ConstantBootstraps#invoke}
   * in order to initialise a dynamic constant value. The constructor should be executed exactly once,
   * no matter how many times {@link CondyCallable#call} is called.
   */
  public Application() {
    System.out.println("Sample instance created");
  }
}
