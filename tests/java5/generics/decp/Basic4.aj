// Works, Double meets the spec for the type parameter
interface I<T extends Number> { }

public class Basic4 {
  public static void main(String[] argv) {
    Basic4 b4 = new Basic4();
    if (!(b4 instanceof I)) 
      throw new RuntimeException("Should be instanceof I!");
  }
}

aspect X {
    declare parents: Basic4 implements I<Double>;
}
