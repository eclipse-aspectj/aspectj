import java.util.*;

public class MethodP {
  public static void main(String []argv) { }
}

class SimpleClass<N extends Number> { }

aspect X {
  public static List<N> SimpleClass<N>.m() {return null;}  // error, static members in generic types cannot use the type variables
}
