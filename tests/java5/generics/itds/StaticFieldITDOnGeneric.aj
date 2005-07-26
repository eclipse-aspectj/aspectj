/*
 * Static ITD of a field onto a generic type that utilises
 * a type variable from the target generic
 */
import java.util.*;

class MathUtils<N> { 

}

public class StaticFieldITDOnGenericType {
  public static void main(String[] argv) {
    MathUtils<Integer>.n=42;
    System.err.prinltn(">"+MathUtils<Integer>.n);
  }
}

aspect X {
  static E MathUtils<E>.n;
}
