public class pr109728 {

  public static void main(String[]argv) {
    new pr109728().m().clone();
  }

  int[] m() { return null; }
}



aspect X {
  before(): call(* *(..)) {}
}
