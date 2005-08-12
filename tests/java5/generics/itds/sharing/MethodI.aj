import java.util.*;

public class MethodI {
  public static void main(String []argv) {
    Base<Integer> base    = new Base<Integer>();
    List<String> ls = base.m(); // error, violates bounds
  }
}

class Base<N extends Number> { }

aspect X {
  public List<Z> Base<Z>.m() { return null;} 
}
