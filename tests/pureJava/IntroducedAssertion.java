import org.aspectj.testing.Tester;

/** @testcase PUREJAVA PR#725 asserts in aspect and declared methods */
public class IntroducedAssertion {
    public static void main (String[] args) {
        IntroducedAssertion.class.getClassLoader().setClassAssertionStatus("A", true);
        IntroducedAssertion.class.getClassLoader().setClassAssertionStatus("B", true);

        boolean result = false;
        try {
            C.method(null);
        } catch (AssertionError e) {
            result = true;
        }
        Tester.check(result, "no assert: C.method(null)");

        result = false;
        try {
            new C(); // field initializer
        } catch (AssertionError e) {
            result = true;
        }
        Tester.check(result, "no assert: new C()");

        result = false;
        try {
            new D().method(null);
        } catch (AssertionError e) {
            result = true;
        }
        Tester.check(result, "no assert: new D().method(null)");
    } 
}

class C {}
class D {}

aspect B {
    int C.i = method(null);

    // assertion in any introduced code conflicts with any local
    static int C.method( Object o) {
      assert o != null ;
      return 0;
    }
}
aspect A {

    void D.method( Object o) {
      assert o != null ;
    }

    // assertion in any introduced method conflicts with any local
    void method() {
        assert null != System.getProperty("java.version");
    }

    // XXX build test cases for other local variants - these work
    /*
    static {
        assert null != System.getProperty("java.version");
    }
    static void aStaticMethod( Object parameter ) {
      assert parameter != null ;
    }
    */
}

