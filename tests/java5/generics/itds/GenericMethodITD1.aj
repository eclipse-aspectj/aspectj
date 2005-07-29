import java.util.*;

class Base { }

public class GenericMethodITD1 {

  public static void main(String[] argv) {
    List<A> as = new ArrayList<A>();
    new Base().simple(as); // this is OK, <E>s upper bound is object
  }
}

class A {}

aspect X {
  <E> void Base.simple(List<? extends E> list) {}
}
