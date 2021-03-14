public class Code {
  public static void main(String[] argv) {
    // Caveat: Putting the closing '"""' on a separate line adds a line break and 'println' (not 'print'!) adds another.
    System.out.println("""
		       this is a text
                       block
                       """);
  }
}
