import java.util.*;

public class MethodA2 {
  public static void main(String []argv) {
    Base<Integer> base    = new Base<Integer>();
    List<Integer> intList = base.m();
  }
}

class Base<N extends Number> { }

aspect X {
  public List<Z> Base<Z>.m() { // OK, Z becomes N in return type
    return null;
  }; 
}
