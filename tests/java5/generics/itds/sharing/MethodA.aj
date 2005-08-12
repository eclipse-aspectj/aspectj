import java.util.*;

public class MethodA {
  public static void main(String []argv) {
    Base<Integer> base    = new Base<Integer>();
    List<Integer> intList = new ArrayList<Integer>();
    base.m(intList);
  }
}

class Base<N extends Number> { }

aspect X {
  public void Base<Z>.m(List<Z> lz) {}; // OK, Z becomes N in parameter
}
