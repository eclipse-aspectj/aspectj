package primary;

public class Alpha {
  public static void main(String[]argv) {
    try {
      System.err.println("aaa");
    } catch (Throwable t) {
      System.err.println("Caught:"+t);
    }
  }
}
