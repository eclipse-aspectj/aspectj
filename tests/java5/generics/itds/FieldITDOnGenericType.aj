/*
 * Static ITD of a field onto a generic type that utilises
 * a type variable from the target generic
 */
import java.util.*;

class MathUtils<N> { 

}

public class FieldITDOnGenericType {
  public static void main(String[] argv) {
    MathUtils<Integer> mu = new MathUtils<Integer>();
    mu.n=42;
    System.err.println(">"+mu.n);
  }
}

aspect X {
  E MathUtils<E>.n;
}
