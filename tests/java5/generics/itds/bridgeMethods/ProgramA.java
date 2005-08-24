// Bridge methods.

interface I<N extends Number> {
  public N methodOne();
  public N methodTwo();
}

class Impl<T extends Float> implements I<T> {
  public T m() { return null;}
}

public class ProgramA {
  public static void main(String[]argv) {
    Impl i = new Impl();
    i.methodOne();
    i.methodTwo();
  }
}

aspect X {
  public N Impl<N>.methodTwo() { return null;}
}
