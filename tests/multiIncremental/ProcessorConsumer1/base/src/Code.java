public class Code {
  public static void main(String []argv) {
    new Code().run();
  }

  public static void runner() {
    new Code().run();
  }

  public void run() {
    aaa();
    bbb();
    ccc();
    ddd();
  }

  @SuppressWarnings("rawtypes")
  public void aaa() {}

  public void bbb() {}

  @SuppressWarnings("rawtypes")
  public void ccc() {}

  public void ddd() {}
}
