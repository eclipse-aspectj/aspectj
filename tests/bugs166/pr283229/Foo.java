public class Foo implements I {
  public static void main(String []argv) {
    new Foo();
  }
}

interface I {
}


aspect X {
  declare parents: I implements java.io.Serializable;
}
