import org.aspectj.testing.Tester;

public class Driver {
  public static int constructorCount = 0;

  public static void main(String[] args) { test(); }
  
  public static void test() {
    SubClass sub1 = new SubClass();
    // only one constructor has been called
    Tester.checkEqual(constructorCount, 1, "constructor called");
  }
}


class SuperClass {
  public SuperClass() {}
}

class SubClass extends SuperClass {
  public SubClass() {}
}

aspect SuperAspect {
    after () returning(): call(SuperClass+.new(..)) {
        Driver.constructorCount += 1;
    }
}
