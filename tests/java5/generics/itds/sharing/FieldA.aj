import java.util.*;

public class FieldA {
  public static void main(String []argv) {
    Base<Integer> base    = new Base<Integer>();
    List<Integer> intList = new ArrayList<Integer>();
    base.j = intList;
  }
}

class Base<N extends Number> { }

aspect X {
  public List<Z> Base<Z>.j; // OK, Z becomes N
}
