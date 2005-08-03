import java.util.*;

class Base { 
}

public class GenericMethodITD11 {

  public static void main(String[] argv) {
    List<A> as = new ArrayList<A>();
    new Base().crazy(as); // ok
  }
}

interface Foo<T> {
  public void m(T t);
}

class A implements Foo<A> {
  public void m(A a) {}
}

aspect X {
  <R extends Foo<? extends R>> void Base.crazy(List<R> lr) {}
}
