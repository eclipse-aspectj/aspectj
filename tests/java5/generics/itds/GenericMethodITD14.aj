import java.util.*;

class Base { 
}

public class GenericMethodITD14 {

  public static void main(String[] argv) {
    List<A> as = new ArrayList<A>();
    new Base().crazy(as); // bad.  A implements Foo<C> but C is a 
                          //       subclass of A, not a superclass
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
  <R extends Foo<? super R>> void Base.crazy(List<R> lr) {}
}
