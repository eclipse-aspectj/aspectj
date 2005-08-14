import java.util.*;

public class MethodJ {
  public static void main(String []argv) {
    One o = new One();
    List l = o.m();
  }
}

interface I<N extends Number> { }

class One implements I {}

aspect X {
  public List I.m() { return null;} // ok, very simple ITD on generic type, should be available for call on One
}
