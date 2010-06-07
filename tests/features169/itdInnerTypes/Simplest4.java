public class Simplest4 {
 // static String abc = "hello world";
  public static void main(String []argv) {
    System.out.println(Inner.number);
  }
}

aspect X {
  public static class Simplest4.Inner {
    static int number = 42;
    public void m() {
//      System.out.println(abc);
    }
  }
}
