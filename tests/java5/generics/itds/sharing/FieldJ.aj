// This test covers something that arose whilst working on specifying
// ITDs on generic interfaces (which must be picked up by the top most
// implementing types).  Here we check that a simple ITD on a generic
// interface works when the implementing class just references it in
// its raw form (see class 'One' below)
import java.util.*;

public class FieldJ {
  public static void main(String []argv) {
    One o = new One();
    o.i = new ArrayList();
  }
}

interface I<N extends Number> { }

class One implements I {}

aspect X {
  public List I.i;
}
