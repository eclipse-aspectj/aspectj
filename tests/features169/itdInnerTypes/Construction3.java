aspect Aspect2 {
  public static class Construction3.__ {
    String string;
    public __(String string) { this.string = string;}
    public String toString() {
      return string;
    }
  }
  public static Construction3.__ Construction3.__() { return new __("abc"); }
  public static String Construction3.foo() { return "abc"; }
}
public class Construction3 {
  public static void main(String []argv) {
    Object o = __();
    o = foo();
    System.out.println(o);
  }
}
