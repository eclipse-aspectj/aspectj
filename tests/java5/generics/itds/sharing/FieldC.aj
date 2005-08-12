import java.util.*;

public class FieldC {
  public static void main(String []argv) {
    Base<Integer> base    = new Base<Integer>();
    List<List<Integer>> intList2 = new ArrayList<List<Integer>>();
    base.j=intList2;

  }
}

class Base<N extends Number> { 
}

aspect X {
  public List<List<Z>> Base<Z>.j; // ok - nested but OK
}
