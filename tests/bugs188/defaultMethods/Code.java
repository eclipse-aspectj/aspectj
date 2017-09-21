public class Code implements I {
  public static void main(String[] argv) {
    new Code().m();
  }
}
aspect X {
  void around(): execution(* I.*()) {
    System.out.println("a");
    proceed();
  }
}

interface I {
  default void m(){
    System.out.println("b");
  }
}

