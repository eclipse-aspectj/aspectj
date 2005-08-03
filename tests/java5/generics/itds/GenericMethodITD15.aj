import java.util.*;

class Base { 
}

public class GenericMethodITD15 {

  public static void main(String[] argv) {
    List<A> as = new ArrayList<A>();
    new Base().crazy(as); // ok. A implements Foo<C> and C is a superclass of A
  }
}

interface Foo<T> {
  public void m(T t);
}


class C {}

class B extends C {}

class A extends B implements Foo<C> {
  public void m(C a) {}
}

aspect X {
  <R extends Foo<? super R>> void Base.crazy(List<R> lr) {}
}
