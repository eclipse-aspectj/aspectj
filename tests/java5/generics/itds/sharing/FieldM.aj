import java.util.*;

public class FieldM {
  public static void main(String []argv) {
    List<Double> doubleList = new ArrayList<Double>();
    List<Float>  floatList  = new ArrayList<Float>();

    One o = new One();
    o.i = new ArrayList();

    Two t = new Two();
    t.i = floatList;
  }
}

interface I<N extends Number> { }

class One implements I<Double> {}

class Two implements I<Float> {}

aspect X {
  public List<Z> I<Z>.i; // ok
}
