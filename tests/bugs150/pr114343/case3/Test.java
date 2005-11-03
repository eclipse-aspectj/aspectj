import java.util.*;

public class Test<T extends Number> {

  Set<T> set = new HashSet<T>();
  T t = null;

  public <T> T[] toArray(T[] a) {
    System.err.println("In toArray()");
    return set.toArray(a);
  }

  public T getFirst() {
    return t;
  }
}
