import java.util.*;

public class FieldI {
  public static void main(String []argv) {
    Base<Integer> base    = new Base<Integer>();
    List<String> strList = new ArrayList<String>();
    base.j = strList; // error, violates bounds
  }
}

class Base<N extends Number> { }

aspect X {
  public List<Z> Base<Z>.j; // ok
}
