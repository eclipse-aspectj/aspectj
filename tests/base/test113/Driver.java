import org.aspectj.testing.Tester;

public class Driver {
  public static boolean classStaticInitRan, classInitRan;
  
  public static boolean aspectStaticInitRan, aspectInitRan;
  
  public static void main(String[] args) { test(); }
  
  public static void test() {
    Class  c = new Class();
    Tester.check(classStaticInitRan, "ran class's static initializer");
    Tester.check(classInitRan, "ran class's initializer");
    Tester.check(aspectStaticInitRan, "ran aspect's static initializer");
    Tester.check(aspectInitRan, "ran aspect's initializer");

  }
}

class Class {
  static {
    Driver.classStaticInitRan = true;
  }
  
  {
    Driver.classInitRan = true;
  }
}

aspect Aspect pertarget(target(Class)) {
  static {
    Driver.aspectStaticInitRan = true;
  }
  // non-static initializer
  {
    Driver.aspectInitRan = true;
  }
}
