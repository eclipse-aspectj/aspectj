// error, can't implement two variants of a generic type
interface I<T>{ }

public class Basic2 implements I<String> {

  public static void main(String[]argv) {
  }
}

aspect X{
    declare parents: Basic2 implements I<Integer>; // error
}
