interface I<T>{ }

public class Basic{

  public static void main(String[]argv) {
    Basic b = new Basic();
    if (!(b instanceof I)) throw new RuntimeException("Should implement I??");
  }
}

aspect X{
    declare parents: Basic implements I<String>;
}
