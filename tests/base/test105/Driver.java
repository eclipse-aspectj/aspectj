import org.aspectj.testing.Tester;

public aspect Driver {

  static boolean t1, t2, t3, t4;

  public static void main(String[] args) { test(); }

  public static void test() {

    Foo f1 = new Foo();

    f1.m1("foo");
    f1.m2(1);    
    f1.m3("foo");    
    f1.m3(1);    

    Tester.check(t1, "finding m1(String)");
    Tester.check(t2, "finding m2(int)");
    Tester.check(t3, "finding m3(String)");
    Tester.check(t4, "finding m3(int)");
  }

   before(String x): target(Foo) && call(void m1(String)) && args(x) {
      t1 = true;
  }

   before(int x): target(Foo) && call(void m2(int)) && args(x) {
      t2 = true; 
  }

   before(String x): target(Foo) && call(void m3(String)) && args(x) {
      t3 = true; 
  }

   before(int x): target(Foo) && call(void m3(int)) && args(x) {
      t4 = true; 
  }
}

class Foo {
  void m1(String x) { }
  void m2(int x) { }
  void m3(String x) { }
  void m3(int x) { }
}
