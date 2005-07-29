import java.util.*;

class Base { }

public class GenericMethodITD4 {

  public static void main(String[] argv) {
    List<A> as = new ArrayList<A>();
    List<B> bs = new ArrayList<B>();
    new Base().simple(as,bs);
  }

  class A {}
  class B extends A {}
}


aspect X {
  <A,B> void Base.simple(List<A> one,List<B> two) {}
}
