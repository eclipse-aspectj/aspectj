// Calling an ITD'd method where the parameterization is '? extends Super'
import java.util.*;

class Base { }

public class ParameterizedMethodITD1 {

  public static void main(String[] argv) {
    List<A> as = new ArrayList<A>();
    new Base().simple(as);
  }
}

class Super {}

class A extends Super {}


aspect X {
  void Base.simple(List<? extends Super> list) {}
}
