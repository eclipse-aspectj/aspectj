public class FieldAccess {
 // static String abc = "hello world";
  public static void main(String []argv) {
    System.out.println(Inner.number);
  }
}

aspect X {
  public static class FieldAccess.Inner {
    static int number = 42;
    public void m() {
//      System.out.println(abc);
    }
  }
}
