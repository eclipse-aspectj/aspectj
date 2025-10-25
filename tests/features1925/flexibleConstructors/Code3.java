public class Code3 {
  public static void main(String[] argv) {
    new Code3();
  }

  Code3() {
    System.out.println("hello");
    super();
  }
}

aspect X {
before(): preinitialization(Code3.new()) {
  System.out.println("before");
}
}
