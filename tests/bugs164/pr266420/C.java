public class C {
  private int i;
  private int j;
}

privileged aspect X {
  public void C.m() {
    i = 5;
  }
}
