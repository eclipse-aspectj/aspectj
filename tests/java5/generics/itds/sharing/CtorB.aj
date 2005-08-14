import java.util.*;

public class CtorB {
  public static void main(String []argv) {
    Base<Integer,String> base    = new Base<Integer,String>();

  }
}

class Base<N extends Number,M> { 

}

aspect X {
  public Base<Z>.new(List<Z> lz) {} // CE only supplied 1 type parameter
}
