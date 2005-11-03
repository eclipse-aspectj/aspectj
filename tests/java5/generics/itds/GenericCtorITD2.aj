import java.util.*;

class Base { 
// <T> Base(List<T> one,List<? extends T> two) { }
}

public class GenericCtorITD2 {
  public static void main(String[] argv) {
    List<A> as = new ArrayList<A>();
    List<B> bs = new ArrayList<B>();
    new Base(as,bs); // ok
  }

  class A {}
  class B extends A {}
}


aspect X {
  // wildcards, argh!
  <T> Base.new(List<T> one,List<? extends T> two) { this(); }
}
