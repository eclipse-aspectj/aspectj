import java.util.*;

public class MethodM {
  public static void main(String []argv) {
    List<Double> doubleList = new ArrayList<Double>();
    List<Float>  floatList  = new ArrayList<Float>();

    One o = new One();
	List<Double> ld = o.m();

    Two t = new Two();
	List<Float>  lf = t.m();
  }
}

interface I<N extends Number> { }

class One implements I<Double> {}

class Two implements I<Float> {}

aspect X {
  public List<Z> I<Z>.m() { return null;} // ok
}
