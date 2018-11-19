public class Code2 {
  public static void main(String[] argv) {
    new Code2().run();
  }

  public void run() {
    new Helper2().run();
  }
}

abstract class Helper {
  public void run() {
  }
}

class Helper2 extends Helper {
}

aspect X {
  before(): execution(* (is(AbstractType)).run(..)) {
    System.out.println(thisJoinPointStaticPart);
  }
}

