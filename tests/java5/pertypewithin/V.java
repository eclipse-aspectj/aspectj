public class V {

  public static int staticVar;
  public int      instanceVar;

  public static void main(String[] argv) {
    new V().m();
  }

  public void m() {
    staticVar = 4;
    instanceVar = 5;
  }

  static {
    System.err.println("In static initializer");
  }

}

aspect Vaspect pertypewithin(V) {

  before(): within(*) { }
}
