import java.util.*;

public class MethodO {
  public static void main(String []argv) {
  }
}

interface I<N> { }

aspect X {
  public List<String> I<String>.m() {}; // error, String is an exact type
}
