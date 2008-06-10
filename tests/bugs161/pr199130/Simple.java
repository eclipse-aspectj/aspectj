interface A {}
abstract aspect Parent< T > {
  public void m(T i) {}
}
abstract aspect Child< V extends A > extends Parent< V > {
  public void n(V i) {}
}

public class Simple {
  public static void main(String []argv) {
  }
}
