public class Code {
  public static void main(String []argv) {
    foo("fooname");
    bar("crap","barname");
  }

  public static void foo(String username) {}

  public static void bar(String a, String username) { }
}

aspect X {
  before(String username): (execution(public static * foo(..)) && args(username,..)) ||
                           (execution(public static * bar(..)) && args(*,username,..)) {
    System.out.println("username = "+username);
  }
}
