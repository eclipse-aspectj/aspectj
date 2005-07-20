interface I<T>{ }
interface K<T>{ }

public class Basic6<J> implements I<J> {

  public static void main(String[]argv) {
    Basic6 b6 = new Basic6();
    if (!(b6 instanceof I)) 
      throw new RuntimeException("Basic6 should be instanceof I");
    if (!(b6 instanceof K)) 
      throw new RuntimeException("Basic6 should be instanceof K");
  }

}

aspect X {
  declare parents: Basic6 implements K<Integer>;
}
