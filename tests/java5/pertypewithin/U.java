public class U {

  public static int staticVar;
  public int      instanceVar;

  public static void main(String[] argv) {
    new U().m();
  }

  public void m() {
    staticVar = 4;
    instanceVar = 5;
  }

  static {
    System.err.println("In static initializer");
  }

}

aspect Uaspect {

  before(): within(U) { }
}
