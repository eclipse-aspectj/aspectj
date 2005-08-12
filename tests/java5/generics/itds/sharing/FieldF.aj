import java.util.*;

public class FieldF {
  public static void main(String []argv) {
    Base<Double,String> base = new Base<Double,String>();
    base.j= new HashMap<String,Double>();
  }
}

class Base<N extends Number,S> { 
}

aspect X {
  public Map<A,B> Base<B,A>.j;  // ok multiple reversed
}
