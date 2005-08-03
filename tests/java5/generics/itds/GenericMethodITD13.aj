import java.util.*;

class Base { 
}

public class GenericMethodITD13 {

  public static void main(String[] argv) {
    List<A> as = new ArrayList<A>();
    new Base().crazy(as); // ok.  A implements Foo<C> where C extends A !!
  }
}

interface Foo<T> {
  public void m(T t);
}

class C extends A { }

class A implements Foo<C> {
  public void m(C a) {}
}

aspect X {
  <R extends Foo<? extends R>> void Base.crazy(List<R> lr) {}
}
