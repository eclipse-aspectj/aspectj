/*
 * Static ITD of a method onto a generic type that utilises
 * a type variable from the target generic
 */
import java.util.*;

class MathUtils<N> { 

  public N doubleIt(N n) {
    int i = Integer.parseInt(n);
    return i*2;
  }

}

public class StaticMethodITDOnGenericType {
  public static void main(String[] argv) {
    List<Integer> ints = new ArrayList<Integer>();
    ints.add(10); 
    System.err.println("Double(10)=" + MathUtils<Integer>.doubleIt(5));
    System.err.println("First="      + MathUtils.first(ints));
  }
}


aspect X {
  // Using the type variable from the type. this is not a generic method.
  static E MathUtils<E>.first(List<E> elements) { return elements.get(0); }
}
