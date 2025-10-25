public class Code4 {
  public static void main(String[] argv) {
    new Code4();
  }

  Code4() {
    System.out.println("hello");
    super();
  }
}

aspect X {
before(): initialization(Code4.new()) {
  System.out.println("before");
}
}
