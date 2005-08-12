import java.util.*;

public class MethodH {
  public static void main(String []argv) {
    Base<Integer> baseInt    = new Base<Integer>();
    Base<String>  baseString = new Base<String>();

    List<Integer> intList = new ArrayList<Integer>();
    List<String>  strList = new ArrayList<String>();
    List<Integer> li = baseInt.m(intList);
    List<String>  ls = baseString.m(strList);
  }
}

class Base<N> { }

aspect X {
  public List<Z> Base<Z>.m(List<Z> lz) { return null;} // OK, Z becomes N in first case, S in the second ;)
}
