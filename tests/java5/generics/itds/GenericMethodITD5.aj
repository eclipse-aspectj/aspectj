import java.util.*;

class Base { }

public class GenericMethodITD5 {

  public static void main(String[] argv) {
    List<A> as = new ArrayList<A>();
    List<B> bs = new ArrayList<B>();
    new Base().simple(as,bs);// error, first is List<A>, second is List<B>
  }

}

class A {}
class B extends A {}

aspect X {
  <E> void Base.simple(List<E> one,List<E> two) {}
}
