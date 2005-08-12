// This test covers something that arose whilst working on specifying
// ITDs on generic interfaces (which must be picked up by the top most
// implementing types).  

// Here we check that a simple ITD on a generic interface works when
// the implementing class is using it in some parameterized form.
import java.util.*;

public class FieldK {
  public static void main(String []argv) {
    One o = new One();
    o.i = new ArrayList();
  }
}

interface I<N extends Number> { }

class One implements I<Double> {}

aspect X {
  public List I.i;
}
