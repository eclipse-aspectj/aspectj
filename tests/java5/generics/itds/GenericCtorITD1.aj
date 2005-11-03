import java.util.*;

class Base { }

public class GenericCtorITD1 {
  public static void main(String[] argv) { 
    // Use the generic ctor
    List<String> ls = new ArrayList<String>();
    Base bs = new Base(ls); // ok
    List<Integer> li = new ArrayList<Integer>();
    Base bi = new Base(li); // ok
  }
}


aspect X {
  <T> Base.new(List<T> elements) { this(); }
}
