public class Middle1 {
  protected void publicMethod() {
  }

  void protectedMethod() {
  }

  private void defaultMethod() {
  }

  private void privateMethod() {
  }

  public static void main(String[] argv) {
    Middle1 m = new Middle1();

    m.publicMethod();
    m.protectedMethod();
    m.defaultMethod();
    m.privateMethod();
  }
}
