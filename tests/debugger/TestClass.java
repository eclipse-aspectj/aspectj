//package debugger;

public class TestClass {
  public static void main(String[] args) {
    new TestClass().go();
  }

  void go() {
    String s = "s";
    String ss = "ss";
    String sss = "sss";
    a();
  }
  
  void a() {
    int i3 = 3;    
    b();
  }

  void b() {
    int i1 = 1;
    int i2 = 2;
    c();
  }

  void c() {
    String c = "c";
    System.out.println(c);
  }
  
}

aspect TestClassAspect of eachobject(instanceof(TestClass)) {
  pointcut a(): receptions(* a(..)) && instanceof(TestClass);
  pointcut b(): receptions(* b(..)) && instanceof(TestClass);
  pointcut c(): receptions(* c(..)) && instanceof(TestClass);
  before(): a() {
    System.out.println("before a");
  }
  before(): b() {
    System.out.println("before b");
  }
  before(): c() {
    System.out.println("before c");
  }
  after(): a() {
    long l = 123;
    System.out.println("after a");
  }
  after(): b() {
    System.out.println("after b");
  }
  after(): c() {
    System.out.println("after c");
  }  
}
