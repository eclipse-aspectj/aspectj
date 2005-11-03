import java.util.*;

public class CtorI {
  public static void main(String []argv) {
    List<String> ls = new ArrayList<String>();
    Base b = new Base(ls); // error, violates bounds
  }
}

class Base<N extends Number> { }

aspect X {
  public Base<Z>.new(List<Z> lz) {this();}
}
