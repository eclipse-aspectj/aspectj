
import pack3.Foo;
import org.aspectj.testing.Tester;

public class Driver {
  public static void main(String[] args) { test(); }
  
  public static void test() {
    C1 c1 = new C1();
    C2 c2 = new C2();
    C3 c3 = new C3();

    Tester.checkEqual(c1.m(), 3, "c1.m()");
    Tester.checkEqual(c2.m(), 3, "c2.m()");
    Tester.checkEqual(c3.m(), 3, "c3.m()");
  }
}

class C3 {
  int m () {
    Foo f = new Foo();
    return f.hello();
  }
}

aspect Test {
     int around(): ( target(C1) || 
                                       target(C2)   ) && 
                   call(int m()) {
        Foo f = new Foo();
        return f.hello();
    }
}
