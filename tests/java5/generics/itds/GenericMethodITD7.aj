import java.util.*;

class Base { }

public class GenericMethodITD7 {

  public static void main(String[] argv) {
    List<Number> ns = new ArrayList<Number>();
    List<Double> ds = new ArrayList<Double>();
    new Base().simple(ns,ds); // ok, List<Number> and List<Double>
  }
}

class Super {}

class A extends Super {}

aspect X {
  <E> void Base.simple(List<E> lista,List<? extends E> listb) {}
}
