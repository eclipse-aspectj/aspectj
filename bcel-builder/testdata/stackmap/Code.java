public class Code {
  public void method1() {
    int i = 1;
    if (true) {
      System.out.println("x");
    }
    i=2;
    try {
      int j = 5;
      i=3;
      System.out.println("y");
      System.out.println("z");
    } catch (Throwable t) {
      System.out.println("e");
      int k=3;
    }
    if (true) {
      i = 4;
      System.out.println("a");
    }
  
  }
}
