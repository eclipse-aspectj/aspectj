import java.util.*;

public class MethodC {
  public static void main(String []argv) {
    Base<Integer> base    = new Base<Integer>();
    List<List<Integer>> intList2 = new ArrayList<List<Integer>>();
    base.m(intList2);
  }
}

class Base<N extends Number> { 
}

aspect X {
  public void Base<Z>.m(List<List<Z>> llz) {}; // ok - nested but OK
}
