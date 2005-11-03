import java.util.*;

public class TTT {
  public void foo() {
    System.err.println("Creating Test<Integer> instance");
    Test<Integer> mt = new Test<Integer>();
    System.err.println("Calling toArray");
    Integer[] arr = mt.toArray(new Integer[]{});
    System.err.println("done");
  }
}
