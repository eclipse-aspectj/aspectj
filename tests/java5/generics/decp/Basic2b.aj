interface I<T>{ }

public class Basic2b implements I {

  public static void main(String[]argv) {
  }
}

aspect X{
    declare parents: Basic2b implements I<Integer>; // error
}
