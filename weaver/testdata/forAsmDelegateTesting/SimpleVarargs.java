
public class SimpleVarargs {
  public static void main(String[] argv) {
    callfoo("a","b","c","d","e");
  }

  public static void callfoo(Object... args) {
    for (int i = 0 ; i<args.length;i++) {
      System.err.println(args[i]);
    }
  }
}
