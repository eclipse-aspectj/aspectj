import java.util.*;

public class FieldP {
  public static void main(String []argv) { }
}

class SimpleClass<N extends Number> { }

aspect X {
  public static List<N> SimpleClass<N>.i;  // error, static members in generic types cannot use the type variables
}
