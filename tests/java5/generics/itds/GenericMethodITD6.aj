import java.util.*;

class Base { }

public class GenericMethodITD6 {

  public static void main(String[] argv) {
    List<Double> as = new ArrayList<Double>();
    new Base().simple(as); // ok, Double extends Number
  }
}

class Super {}

class A extends Super {}

aspect X {
  <E extends Number> void Base.simple(List<? extends E> list) {}
}
