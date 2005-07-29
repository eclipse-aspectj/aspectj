import java.util.*;

class Base { }

public class ParameterizedMethodITD4 {

  public static void main(String[] argv) {
    List<A> as = new ArrayList<A>();
    new Base().simple(as); // ok, A is a supertype of B
  }
}

class A {}

class B extends A {}


aspect X {
  void Base.simple(List<? super B> list) {}
}
