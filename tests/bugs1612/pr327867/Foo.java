public class Foo {

  public static void main(String[]argv) {
    new Foo().m();
  }

  public void m() {
    int j = new Bar().i;
  }

  private class Bar {
    private int i;
  }
}

aspect X {
  before(): execution( !synthetic * *(..)) {}
}
