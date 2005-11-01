public class SameTypeVariable {

  public static void main(String[]argv) {
    new SimpleClass<Double>();
  }

}


class SimpleClass<N extends Number> {

}

aspect X {

  public N SimpleClass<N>.get() { return null; }
//  public P SimpleClass<P>.get() { return null; }

}
