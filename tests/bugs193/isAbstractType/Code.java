public class Code {
  public static void main(String[] argv) {
    new Code().run();
  }

  public void run() {
    new Code3().run();
  }
}

abstract class Code2 {
  public void run() {
  }
}

class Code3 extends Code2 {
}

aspect X {
  before(): execution(* (!is(AbstractType)).run(..)) {
    System.out.println(thisJoinPointStaticPart);
  }
}

