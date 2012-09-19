public class Clazz2 {
  public static void main(String[] argv) {
    Bar bs = new Bar();
    String s = bs.bar("abc",null);
  }
}

class Bar implements Code2.I<String> {
}
