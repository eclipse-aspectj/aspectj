
import org.aspectj.testing.Tester;

/** @testcase PUREJAVA compiling asserts using 1.3 (requires ajc run under JDK 1.3) */
public class AssertInMethod {
    public static void main (String[] args) {
        AssertInMethod.class.getClassLoader().setClassAssertionStatus("C", true);
        boolean result = false;
        try {
            new C().internalMethod(null);
        } catch (AssertionError e) {
            result = true;
        }
        Tester.check(result, "assert not thrown");
    } 
}

class C { 
    void internalMethod( Object o) {
      assert o != null ;
    }
}
