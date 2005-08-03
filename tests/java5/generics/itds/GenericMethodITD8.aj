import java.util.*;

class Base { }

public class GenericMethodITD8 {

  public static void main(String[] argv) {
    List<Number> ns = new ArrayList<Number>();
    List<String> ss = new ArrayList<String>();
    new Base().simple(ns,ss); // error, ss doesn't fit List<? extends E>
  }
}

class Super {}

class A extends Super {}

aspect X {
  <E> void Base.simple(List<E> list1,List<? extends E> list2) {}
}
