import java.util.*;

public class MethodA3 {
  public static void main(String []argv) {
    Base<Integer> base    = new Base<Integer>();
    List<Integer> intList = new ArrayList<Integer>();
    intList = base.m(intList);
  }
}

class Base<N extends Number> { }

aspect X {
  public List<Z> Base<Z>.m(List<Z> lz) { // OK, Z becomes N in return type and parameter
    return lz;
  }; 
}
