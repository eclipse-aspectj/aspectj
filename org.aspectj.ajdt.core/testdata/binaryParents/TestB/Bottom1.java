public class Bottom1 extends Top1 {
  public void publicMethod() {
  }

  protected void protectedMethod() {
  }

  void defaultMethod() {
  }

  private void privateMethod() {
  }

  public static void main(String[]argv) {
    Bottom1 b = new Bottom1();
    b.publicMethod();
    b.protectedMethod();
    b.defaultMethod();
    b.privateMethod();
  }
}
