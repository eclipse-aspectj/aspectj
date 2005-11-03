import java.util.*;

public class CtorF {
  public static void main(String []argv) {

    Base b =  new Base(new HashMap<String,Float>(),new HashMap<Double,Float>());
  }
}

class Base<P,N extends Number,S> { 
}

aspect X {
  public Base<A,B,C>.new(Map<C,A> lc,Map<B,A> lb) { this(); }  // ok multiple in funny orders 
}
