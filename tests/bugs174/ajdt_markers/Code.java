public class Code extends A implements I {
  
  public static void main(String[] argv) {
    Code code = new Code();
    code.am();
    code.im();
  }
}

class A {
  public void am() {}
}

interface I {
}

aspect X {
  public void I.im() {}
  before(): call(* A+.*m(..)) {}
}
