import java.util.*;

public class MethodF {
  public static void main(String []argv) {
    Base<Float,Double,String> base = new Base<Float,Double,String>();

    Map<Double,Float> mii = base.m(new HashMap<String,Float>());
  }
}

class Base<P,N extends Number,S> { 
}

aspect X {
  public Map<B,A> Base<A,B,C>.m(Map<C,A> lc) { return null;}  // ok multiple in funny orders 
}
