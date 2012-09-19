public class Clazz4 {
  public static void main(String[] argv) {
    Bar<String> bs = new Bar<String>();
    String s = bs.bar("abc",new Integer(4));
  }
}

class Bar<A extends java.io.Serializable> implements Code4.I<A> {
}
