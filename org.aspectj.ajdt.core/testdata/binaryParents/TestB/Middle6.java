public class Middle6 {
  protected void publicMethod() {
  }

  void protectedMethod() {
  }

  private void defaultMethod() {
  }

  private void privateMethod() {
  }


  private void anotherPublicMethod() { }
  private void anotherProtectedMethod() {}

  public static void main(String[] argv) {
    Middle6 m = new Middle6();

    m.publicMethod();
    m.protectedMethod();
    m.defaultMethod();
    m.privateMethod();
  }
}
