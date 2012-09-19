public class Clazz3 {
  public static void main(String[] argv) {
    Bar<String> bs = new Bar<String>();
    String s = bs.bar("abc",null);
  }
}

class Bar<A extends java.io.Serializable> implements Code3.I<A> {
}
