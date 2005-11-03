import java.util.*;

public class CtorA {
  public static void main(String []argv) {
    List<Integer> intList = new ArrayList<Integer>();
    Base<Integer> base    = new Base<Integer>(intList);
  }
}

class Base<N extends Number> { 
  public Base() {}
  <Y extends Number> Base(Set<N> sn, List<Y> ys) {}
}

aspect X {
  public Base<Z>.new(List<Z> lz) { this(); } // OK, Z becomes N in parameter
}
