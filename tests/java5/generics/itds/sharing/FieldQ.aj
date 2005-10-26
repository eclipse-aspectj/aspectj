import java.util.*;

interface I<N extends Number> {
  
}


public class FieldQ implements I<Double> {

  public static void main(String []argv) {
  }
  
}

aspect X {
  Z I<Z>.list;
}
