public class Bottom3 extends Top3 {
  public Bottom3() {
    super();
  }

  public static void main(String[]argv) {
    Bottom3 b = new Bottom3();
    b.runit();
  }

  public void runit() {
    super.m("x");
  }
}
