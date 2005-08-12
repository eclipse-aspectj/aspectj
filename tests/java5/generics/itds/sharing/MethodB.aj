import java.util.*;

public class MethodB {
  public static void main(String []argv) {
    Base<Integer,String> base    = new Base<Integer,String>();
    List<Integer> intList = new ArrayList<Integer>();

  }
}

class Base<N extends Number,M> { 

}

aspect X {
  public List<Z> Base<Z>.m() { return null;}; // CE only supplied 1 type parameter
}
