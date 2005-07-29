import java.util.*;

class Base { }

public class ParameterizedMethodITD2 {

  public static void main(String[] argv) {
    List<A> as = new ArrayList<A>();
    new Base().simple(as); // error, A is not a number...
  }
}

class Super {}

class A extends Super {}


aspect X {
  void Base.simple(List<? extends Number> list) {}
}
