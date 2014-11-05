public class Code2 {
  public static void main(String[] argv) {
    new Code2().run();
  }

  public void run() {
    new Helper().run();
  }
}

final class Helper {
  public void run() {
  }
}

aspect X {
  before(): execution(* (is(FinalType)).run(..)) {
    System.out.println(thisJoinPointStaticPart);
  }
}
