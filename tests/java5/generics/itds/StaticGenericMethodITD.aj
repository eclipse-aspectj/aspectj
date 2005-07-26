/*
 * Static ITD of a generic method 
 */
import java.util.*;

class MathUtils { 
}

public class StaticGenericMethodITD {
  public static void main(String[] argv) {
    List<Integer> ints = new ArrayList<Integer>();
    ints.add(10); ints.add(20); ints.add(30);
    System.err.println("First="+MathUtils.first(ints));
    if (!MathUtils.first(ints).equals(10))
      throw new RuntimeException("First val!=10, it was "+
                                 MathUtils.first(ints));
  }
}


aspect X {
  static <E> E MathUtils.first(List<E> elements) { return elements.get(0); }
}
