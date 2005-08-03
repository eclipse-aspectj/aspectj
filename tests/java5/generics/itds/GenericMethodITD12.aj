import java.util.*;

class Base { 
}

public class GenericMethodITD12 {

  public static void main(String[] argv) {
    List<A> as = new ArrayList<A>();
    new Base().crazy(as); // A doesnt implement Foo<? extends A>
  }
}

interface Foo<T> {
  public void m(T t);
}

class B {}

class A implements Foo<B> {
  public void m(B a) {}
}

aspect X {
  <R extends Foo<? extends R>> void Base.crazy(List<R> lr) {}
}
