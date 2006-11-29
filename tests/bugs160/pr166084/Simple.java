public class Simple {
  public static void main(String []argv) {
    new Simple().m();
  }

  public void m() {
    int i = 1;
    System.out.println(i);
  }
}

aspect X {
  before(): call(* println(..)) {}
}
