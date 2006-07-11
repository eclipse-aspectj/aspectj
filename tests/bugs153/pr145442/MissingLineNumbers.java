public class MissingLineNumbers {
  public static void main(String []argv) {
    new MissingLineNumbers().foo();
  }

  public void foo() {
    System.err.println("hello");
    System.err.println("world");
  }
}

aspect X {
  void around(): call(* foo(..)) {
    new RuntimeException().printStackTrace();
  }
}


