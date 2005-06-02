// error, can't implement two variants of a generic type
interface I<T>{ }

public class Basic implements I<String> {

  public static void main(String[]argv) {
  }
}

aspect X{
    declare parents: Basic implements I<Integer>; // error
}
