import java.util.*;

public class FieldL {
  public static void main(String []argv) {
    List<Double> doubleList = new ArrayList<Double>();
    List<Float>  floatList  = new ArrayList<Float>();

    One<Double> o = new One<Double>();
    o.i = new ArrayList();
  }
}

interface I<N extends Number> { }

class One<Z extends Number> implements I<Z> {}

aspect X {
  public List I.i;
}
