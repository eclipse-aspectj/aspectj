public aspect A {

  public static void a(Object... os) {}
  public static void b(String... ss) {}
  public static void c(Integer... is) {}

  public static void d(Object[] os) {}
  public static void e(String[] ss) {}
  public static void f(Integer[] is) {}


  before(Object[] args): call(* *(Object+...)) && args(args) {
    System.out.println("varargs "+thisJoinPoint);
  }

  before(Object[] args): call(* *(Object+[])) && args(args) {
    System.out.println("arrays  "+thisJoinPoint);
  }

  public static void main(String []argv) {
    a();
    b();
    c();
    d(null);
    e(null);
    f(null);
  }

}
