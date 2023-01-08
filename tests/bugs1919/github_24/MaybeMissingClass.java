public class MaybeMissingClass {
  public static void main(String[] args) {
    f1();
    f2();
  }

  public static MaybeMissingClass f1() {
    System.out.println("MaybeMissingClass.f1");
    return null;
  }

  public static MaybeMissingClass[] f2() {
    System.out.println("MaybeMissingClass.f2");
    return null;
  }
}
