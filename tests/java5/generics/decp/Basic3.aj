// error, only one type parameter for I
interface I<T> { }

public class Basic3 {
  public static void main(String[]argv) {
  }
}

aspect X {
    declare parents: Basic3 implements I<String,Number>;
}
