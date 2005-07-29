import java.util.*;

class Base { }

public class GenericMethodITD2 {

  public static void main(String[] argv) {
    List<A> as = new ArrayList<A>();
    new Base().simple(as); // error
  }
}

class Super {}

class A extends Super {}

aspect X {
  <E extends Number> void Base.simple(List<? extends E> list) {}
}
