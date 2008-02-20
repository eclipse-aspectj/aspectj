
package test;

public class X {
  public static void main(String[]argv) {
    TestClass t = new TestClass();
    if (!(t instanceof Interface1)) throw new RuntimeException("t not instanceof Interface1");
    if (!(t instanceof Interface1TestClass)) throw new RuntimeException("t not instanceof Interface1TestClass");
  }
}

interface Interface1 {}

interface Interface1TestClass {}

aspect TestAspect  {
        declare parents: 
                TestClass implements Interface1;

        declare parents: 
                TestClass && Interface1+ implements Interface1TestClass;
}
class TestClass {}
