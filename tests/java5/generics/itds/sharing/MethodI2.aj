import java.util.*;

public class MethodI2 {
  public static void main(String []argv) {
    Base<Integer> base    = new Base<Integer>();
    List<String> ls = new ArrayList<String>();
    base.m(ls); // error, violates bounds
  }
}

class Base<N extends Number> { }

aspect X {
  public void Base<Z>.m(List<Z> lz) { }
}
