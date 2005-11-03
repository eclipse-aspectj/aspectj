import java.util.*;

public class CtorH {
  public static void main(String []argv) {
    Base<Integer> baseInt    = new Base<Integer>();
    Base<String>  baseString = new Base<String>();

    List<Integer> intList = new ArrayList<Integer>();
    List<String>  strList = new ArrayList<String>();
    Base b1 = new Base(intList);
    Base b2 = new Base(strList);
  }
}

class Base<N> { }

aspect X {
  public Base<Z>.new(List<Z> lz) {this();} // OK, Z becomes N in first case, S in the second ;)
}
