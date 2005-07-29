import java.util.*;

class Base { }

public class GenericMethodITD3 {

  public static void main(String[] argv) {
    List<A> as = new ArrayList<A>();
    List<B> bs = new ArrayList<B>();
    Base.simple(as,bs);
  }

  class A {}
  class B extends A {}
}


aspect X {
  <E> void Base.simple(List<E> one,List<? extends E> two) {}
}
