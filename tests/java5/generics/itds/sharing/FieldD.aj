import java.util.*;

public class FieldD {
  public static void main(String []argv) {
    Base<Double,String> base = new Base<Double,String>();
    base.j= new HashMap<Double,String>();
  }
}

class Base<N extends Number,S> { 
}

aspect X {
  public Map<A,B> Base<A,B>.j;  // ok multiple
}
