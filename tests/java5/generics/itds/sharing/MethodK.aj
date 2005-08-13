import java.util.*;

public class MethodK {
  public static void main(String []argv) {
    One o = new One();
    List l = o.m();
  }
}

interface I<N extends Number> { }

class One implements I<Double> {}

aspect X {
  public List I.m() { return null;} // ok, ITD on a generic type which gets parameterized into One.
}
