public class A {
  public static void main(String []argv) {
    byte[][] bytes = new byte[][]{{0},{1}};
  }
}

aspect X {
  after() returning(Object o) : call(*[][].new(..)) {
    System.err.println("new array: "+o.getClass());
  }
}
