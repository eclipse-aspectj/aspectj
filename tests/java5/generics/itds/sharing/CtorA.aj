import java.util.*;

public class CtorA {
  public static void main(String []argv) {
    List<Integer> intList = new ArrayList<Integer>();
    Base<Integer> base    = new Base<Integer>(intList);
  }
}

class Base<N extends Number> { 
  //public Base(List<N> sn) {}
  <Y extends Number> Base(Set<N> sn, List<Y> ys) {}
}

aspect X {
  public Base<Z>.new(List<Z> lz) {}; // OK, Z becomes N in parameter
}
