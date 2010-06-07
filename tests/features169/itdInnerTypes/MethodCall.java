public class MethodCall {
 // static String abc = "hello world";
  public static void main(String []argv) {
    System.out.println(Inner.m());
  }
}

aspect X {
  public static class MethodCall.Inner {
    static int number = 42;
    public static Integer m() {
      return number;
    }
  }
}
