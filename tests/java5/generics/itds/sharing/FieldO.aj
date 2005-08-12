import java.util.*;

public class FieldO {
  public static void main(String []argv) {
  }
}

interface I<N> { }

aspect X {
  public List<String> I<String>.i; // error, String is an exact type
}
