import org.aspectj.testing.Tester;

public class Driver {
  public static void main(String[] args) { test(); }
  
  public static void test() {
    Class  c = new Class();
    
    c.foo(5);
  }
}
    
class Class {
  void foo(int x) {
    //System.out.println(x);
  }
  int baz(int y) {
    return 2;
  }
}

aspect Aspect pertarget(target(Class)) {
  int bar(int x) { return 5;}

  after (Class c, int z): target(c) && call(* foo(int)) && args(z) {
      // calls a  class  method whose argument is an class method call
      Tester.check(c.baz(bar(6)) == 2, "didn't run class method");
      // calls an class method whose argument is a  class  method call
      Tester.check(bar(c.baz(4)) == 5, "Didn't run aspect method");
    }
}
