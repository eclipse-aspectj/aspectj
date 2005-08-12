import java.util.*;

public class MethodD {
  public static void main(String []argv) {
    Base<Double,String> base = new Base<Double,String>();
    base.m(new HashMap<Double,String>());
  }
}

class Base<N extends Number,S> { 
}

aspect X {
  public void Base<A,B>.m(Map<A,B> mabs) {};  // ok multiple
}
