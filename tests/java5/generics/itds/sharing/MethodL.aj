import java.util.*;

public class MethodL {
  public static void main(String []argv) {
    List<Double> doubleList = new ArrayList<Double>();
    List<Float>  floatList  = new ArrayList<Float>();

    One<Double> o1 = new One<Double>();
    One<Float>  o2 = new One<Float>();
	List a = o1.m();
	List b = o2.m();
  }
}

interface I<N extends Number> { }

class One<Z extends Number> implements I<Z> {}

aspect X {
  public List I.m() { return null; } // ok, available in both parameterizations of One
}
