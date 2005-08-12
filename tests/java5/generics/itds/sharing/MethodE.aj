import java.util.*;

public class MethodE {
  public static void main(String []argv) {
    Base<Integer> base    = new Base<Integer>();
    List<String> stringList = new ArrayList<String>();
    base.m();
  }
}

class Base<N extends Number> { 
}

aspect X {
  public List<String> Base<Z>.m() { return null;}; // ok - specified type variable but didnt use it
}
