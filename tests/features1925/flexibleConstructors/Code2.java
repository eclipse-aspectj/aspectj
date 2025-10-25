public class Code2 {
  public static void main(String[] argv) {
    new Code2();
  }

  Code2() {
    System.out.println("hello");
    super();
  }
}

aspect X {
before(): execution(Code2.new(..)) {
  System.out.println("before");
}
}
