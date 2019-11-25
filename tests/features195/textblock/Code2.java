public class Code2 {
  public static void main(String[] argv) {
  }
}

aspect X {

  before(): execution(* Code2.main(..)) {
    System.out.println("""
		       this is a text
                       block in advice
                       """);
  }
}

