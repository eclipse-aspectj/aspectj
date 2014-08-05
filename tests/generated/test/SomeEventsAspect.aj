package test;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Events for test.Some.
 *
 * @author test.SimpleProcessor
 */
@javax.annotation.Generated("test.SimpleProcessor")
final aspect SomeEventsAspect {

  public static final class SomeOnMethod1Event {
    private Collection<SomeCallbacks.OnMethod1> callbacks = null;

    SomeOnMethod1Event() {
    }

    public void add(SomeCallbacks.OnMethod1 callback) {
      Collection<SomeCallbacks.OnMethod1> callbacksSafe = callbacks;
      if (callbacksSafe == null) {
        callbacksSafe = new ArrayList<>(1);
        callbacks = callbacksSafe;
      }
      callbacksSafe.add(callback);
    }

    public void clean() {
      callbacks = null;
    }
  }

  private static void emit(SomeOnMethod1Event event, Some emmiter) {
    final Collection<SomeCallbacks.OnMethod1> callbacksSafe = event.callbacks;
    if (callbacksSafe == null)
      return;
    for (final SomeCallbacks.OnMethod1 callback : new ArrayList<>(callbacksSafe))
      callback.changed(emmiter);
  }

  @SuppressWarnings("PublicField")
  public final SomeOnMethod1Event test.Some.OnMethod1Event = new SomeOnMethod1Event();

  before(): execution(void test.Some.method1()) {
    final test.Some emmiter = (test.Some) thisJoinPoint.getThis();
    emit(emmiter.OnMethod1Event, emmiter);
  }

}

