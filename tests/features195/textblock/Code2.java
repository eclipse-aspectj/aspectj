public class Code2 {
  public static void main(String[] argv) {
  }
}

aspect X {

  before(): execution(* Code2.main(..)) {
    // Caveat: Putting the closing '"""' on a separate line adds a line break and 'println' (not 'print'!) adds another.
    System.out.println("""
		       this is a text
                       block in advice
                       """);
  }
}
