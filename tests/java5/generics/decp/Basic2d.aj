// OK - since new parent matches
interface I<T>{ }

public class Basic2d implements I<Double> {

  public static void main(String[]argv) {
  }
}

aspect X{
    declare parents: Basic2d implements I<Double>;
}
