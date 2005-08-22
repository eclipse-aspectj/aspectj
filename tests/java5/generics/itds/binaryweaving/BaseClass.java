public class BaseClass<N> {
  static int count = 0;

  public static void main(String[]argv) {
    BaseClass b = new BaseClass();
    b.run1();
    b.run2();
    b.run3();
    System.err.println("Advice count="+count);
  }

  public void run1() {}
  public void run2() {}
  public void run3() {}
}
