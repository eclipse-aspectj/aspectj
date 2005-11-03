import java.util.*;

public class CtorC {
  public static void main(String []argv) {
    List<List<Integer>> intList2 = new ArrayList<List<Integer>>();
    Base b = new Base(intList2);
  }
}

class Base<N extends Number> { 
}

aspect X {
  public Base<Z>.new(List<List<Z>> llz) { this(); }; // ok - nested but OK
}
