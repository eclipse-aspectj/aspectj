public class A {
  private int i;
}

privileged aspect X {
  public int A.foo() {
    return i;
  }
}
