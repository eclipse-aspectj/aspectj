public class Code {
  public static void main(String[] argv) {
    new Code().run();
  }

  public void run() {
    new Code2().run();
  }
}

final class Code2 {
  public void run() {
  }
}

aspect X {
  before(): execution(* (!is(FinalType)).run(..)) {
    System.out.println(thisJoinPointStaticPart);
  }
}
