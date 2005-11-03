import java.util.*;

public class CtorD {
  public static void main(String []argv) {
    Base b = new Base(new HashMap<Double,String>());
  }
}

class Base<N extends Number,S> { 
}

aspect X {
  public Base<A,B>.new(Map<A,B> mabs) { this(); };  // ok multiple
}
