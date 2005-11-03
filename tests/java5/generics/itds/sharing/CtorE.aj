import java.util.*;

public class CtorE {
  public static void main(String []argv) {
    Base<Integer> base    = new Base<Integer>();
    List<String> stringList = new ArrayList<String>();
    Base b = new Base(stringList);
  }
}

class Base<N extends Number> { 
}

aspect X {
  public Base<Z>.new(List<String> ls) { this(); }; // ok - specified type variable but didnt use it (could put a lint warning on this case?)
}
