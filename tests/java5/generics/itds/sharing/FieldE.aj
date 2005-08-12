import java.util.*;

public class FieldE {
  public static void main(String []argv) {
    Base<Integer> base    = new Base<Integer>();
    List<String> stringList = new ArrayList<String>();
    base.j = stringList;
  }
}

class Base<N extends Number> { 
}

aspect X {
  public List<String> Base<Z>.j; // ok - specified type variable but didnt use it
}
