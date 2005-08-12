import java.util.*;

public class FieldH {
  public static void main(String []argv) {
    Base<Integer> baseInt    = new Base<Integer>();
    Base<String>  baseString = new Base<String>();

    List<Integer> intList = new ArrayList<Integer>();
    List<String>  strList = new ArrayList<String>();
    baseInt.j    = intList;
    baseString.j = strList;
  }
}

class Base<N> { }

aspect X {
  public List<Z> Base<Z>.j; // OK, Z becomes N in first case, S in the second ;)
}
