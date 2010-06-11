aspect Aspect2 {
  public static class Construction3._ {
    String string;
    public _(String string) { this.string = string;}
    public String toString() {
      return string;
    }
  }
  public static Construction3._ Construction3._() { return new _("abc"); }
  public static String Construction3.foo() { return "abc"; }
}
public class Construction3 {
  public static void main(String []argv) {
    Object o = _();
    o = foo();
    System.out.println(o);
  }
}
