import java.util.*;

public aspect MethodG {

  // visibility options...
  public  List<Z> Base<Z>.i(List<Z> lz) {return null;} // ok
  private List<Z> Base<Z>.j(List<Z> lz) {return null;} // ok
          List<Z> Base<Z>.k(List<Z> lz) {return null;} // ok

  public static void main(String []argv) {
    Base<Integer> base    = new Base<Integer>();
    List<Integer> intList = new ArrayList<Integer>();
    List<Integer> li1 = base.i(intList);
    List<Integer> li2 = base.j(intList);
    List<Integer> li3 = base.k(intList);
  }
}

class Base<N extends Number> { }
