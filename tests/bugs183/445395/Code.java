import java.util.*;

public class Code {
  public void m() { }
  public static void main(String []argv) {
    new Code().m();
  }
}

aspect X {
  void around(): execution(* m(..)) {
    Arrays.asList(4, 5, 6).forEach((i) -> { System.out.println(i);});
  }
}

