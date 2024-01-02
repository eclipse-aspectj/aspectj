public class MaybeMissingClass {
  public static void main(String[] args) {
    f1();
    f2();
    f3();
    f4();
    f5();
    f6();
  }

  public static MaybeMissingClass f1() {
    System.out.println("MaybeMissingClass.f1");
    return null;
  }

  public static MaybeMissingClass[] f2() {
    System.out.println("MaybeMissingClass.f2");
    return null;
  }

  public static MaybeMissingClass[][] f3() {
    System.out.println("MaybeMissingClass.f3");
    return null;
  }

  public static int f4() {
    System.out.println("MaybeMissingClass.f4");
    return 0;
  }

  public static int[] f5() {
    System.out.println("MaybeMissingClass.f5");
    return new int[2];
  }

  public static int[][] f6() {
    System.out.println("MaybeMissingClass.f6");
    return new int[2][2];
  }
}
