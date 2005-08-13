import java.util.*;

public class MethodO2 {
  public static void main(String []argv) {
  }
}

interface I<N> { }

aspect X {
  public void I<String>.m(List<String> ls) {}; // error, String is an exact type
}
