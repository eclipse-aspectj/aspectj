import java.util.*;

class Base { 
}

public class GenericMethodITD10 {

  public static void main(String[] argv) {
    List<A> as = new ArrayList<A>();
    new Base().crazy(as); // A is not Comparable
  }
}

class A {
}

aspect X {
  <R extends Comparable<? super R>> void Base.crazy(List<R> lr) {}
}
