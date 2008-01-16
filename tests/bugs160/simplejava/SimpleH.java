import java.util.*;

class Base { 

}

public class SimpleH {
  public static void main(String[] argv) {
    List<Double> l1 = new ArrayList<Double>();
    Base b2 = new Base(l1);
  }
}

aspect X {
  public <P extends Number> Base.new(List<P> lr) { this(); }
}
