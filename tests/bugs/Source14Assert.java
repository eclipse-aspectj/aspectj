

public class Source14Assert {
  private void method1() {
    try {
      invoke();
    } catch (Throwable throwable) {
      assert false : throwable;
    }
  }
  private void invoke() {}
  public static void main(String[] args) {}
}