import java.util.*;

class Base { 
}

public class GenericMethodITD9 {

  public static void main(String[] argv) {
    List<A> as = new ArrayList<A>();
    new Base().crazy(as); // ok !
  }
}

class A implements Comparable<A> {
 public int compareTo(A anotherA) {
   return 0;
 } 
}

aspect X {
  public <R extends Comparable<? super R>> void Base.crazy(List<R> lr) {}
}
