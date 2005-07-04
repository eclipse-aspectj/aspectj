interface I<T extends Number>{ }
interface J<T extends Number>{ }

public class Basic implements J<Double> {

  public static void main(String[]argv) {
    Basic b = new Basic();
    if (!(b instanceof J)) throw new RuntimeException("Should implement I??");
    if (!(b instanceof I)) throw new RuntimeException("Should implement J??");
  }
}

aspect X{
    declare parents: Basic implements I<Double>;
}
