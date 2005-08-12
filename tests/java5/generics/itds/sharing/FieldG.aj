import java.util.*;

public aspect FieldG {

  // visibility options...
  public  List<Z> Base<Z>.i; // ok
  private List<Z> Base<Z>.j; // ok
          List<Z> Base<Z>.k; // ok

  public static void main(String []argv) {
    Base<Integer> base    = new Base<Integer>();
    List<Integer> intList = new ArrayList<Integer>();
    base.i = intList;
    base.j  = intList;
    base.k = intList;
  }
}

class Base<N extends Number> { }
