aspect Aspect1 {
  public static Construction4._ Construction4._() { return new _("abc"); }
  public static String Construction4.foo() { return "abc"; }
}
aspect Aspect2 {
  public static class Construction4._ {
    String string;
    public _(String string) { this.string = string;}
    public String toString() {
      return string;
    }
  }
} 
public class Construction4 {
  public static void main(String []argv) {
    Object o = _();
    o = foo();
    System.out.println(o);
  }
}
