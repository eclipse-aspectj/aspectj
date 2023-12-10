import java.awt.*;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Examples taken from <a href="https://openjdk.org/jeps/443">JEP 443</a>
 */
public class UnnamedPatternsPreview1 {
  public static void main(String[] args) {
    // An enhanced for loop with side effects
    int acc = 0;
    final int LIMIT = 2;
    for (Order _ : List.of(new Order(), new Order(), new Order())) {
      if (acc < LIMIT)
        acc++;
    }
    System.out.println(acc);

    // The initialisation of a basic for loop can declare unnamed local variables
    for (int i = 0, _ = sideEffect(); i < 2; i++) {
      System.out.println(i);
    }

    // An assignment statement, where the result of the expression on the right hand side is not needed
    Queue<Integer> q = new PriorityQueue<>(List.of(1, 2, 3, 4, 5, 6));
    while (q.size() >= 3) {
      var x = q.remove();
      var y = q.remove();
      var _ = q.remove();
      System.out.println(new Point(x, y));
    }

    // The same unnamed variable name '_' can be used in multiple assignment statements
    q = new PriorityQueue<>(List.of(1, 2, 3, 4, 5, 6));
    while (q.size() >= 3) {
      var x = q.remove();
      var _ = q.remove();
      var _ = q.remove();
      System.out.println(new Point(x, 0));
    }

    // Unnamed variables can be used in one or multiple catch blocks
    String s = "123xy";
    try {
      int i = Integer.parseInt(s);
      System.out.println(i);
    } catch (NumberFormatException _) {
      System.out.println("Bad number: " + s);
    } catch (Exception _) {
      System.out.println("Unexpected error");
    }

    // Try with resources
    try (var _ = ScopedContext.acquire()) {
      System.out.println("Doing something within scoped context");
    }

    // A lambda whose parameter is irrelevant
    System.out.println(
      Stream.of("one", "two", "three")
        .collect(Collectors.toMap(String::toUpperCase, _ -> "NODATA"))
    );
  }

  static int sideEffect() {
    System.out.println("side effect");
    return 42;
  }

  static class Order {}

  static class ScopedContext implements AutoCloseable {
    public static ScopedContext acquire() {
      return new ScopedContext();
    }

    @Override
    public void close() {
      System.out.println("Closing scoped context");
    }
  }
}
