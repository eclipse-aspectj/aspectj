public class BrokenSwitch {
  public static void main(String []argv) {
    int i = 1;
    switch (i) {
      case -845341380: System.err.println("a");break;
      case 1730110032: System.err.println("a");break;
      default: System.err.println("c");break;
    }
  }
}

aspect X {
  before(): execution(* BrokenSwitch.main(..)) {}
}
