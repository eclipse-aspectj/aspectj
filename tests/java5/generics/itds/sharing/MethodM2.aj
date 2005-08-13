import java.util.*;

public class MethodM2 {
  public static void main(String []argv) {
    List<Double> doubleList = new ArrayList<Double>();
    List<Float>  floatList  = new ArrayList<Float>();

    One o = new One();
	o.m(new ArrayList<Double>());

    Two t = new Two();
	t.m(new ArrayList<Float>());
  }
}

interface I<N extends Number> { }

class One implements I<Double> {}

class Two implements I<Float> {}

aspect X {
  public void I<Z>.m(List<Z> lz) {} // ok
}
