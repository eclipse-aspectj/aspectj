aspect Aspect1 {
  public static Construction4.__ Construction4.__() { return new __("abc"); }
  public static String Construction4.foo() { return "abc"; }
}
aspect Aspect2 {
  public static class Construction4.__ {
    String string;
    public __(String string) { this.string = string;}
    public String toString() {
      return string;
    }
  }
} 
public class Construction4 {
  public static void main(String []argv) {
    Object o = __();
    o = foo();
    System.out.println(o);
  }
}
