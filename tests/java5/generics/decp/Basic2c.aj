interface I<T>{ }

public class Basic2c implements I<Double> {

  public static void main(String[]argv) {
  }
}

aspect X{
    declare parents: Basic2c implements I; // error
}
