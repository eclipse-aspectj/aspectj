
import org.aspectj.testing.Tester;

public class AspectInIntroducedMethod {
    public static void main(String[] args) {
        String result = new TargetClass().addMethod();
        Tester.check("inner".equals(result),
                     "\"inner\".equals(\"" + result + "\")");
    }
} 
aspect A {
    class inner {
      public String name() { return "inner"; } 
    }
    /** shows A usable in non-introductions */
    public String getName() {
        new inner();
        A a = this;
        return a.new inner().name();
    }

    // NPE at NewInstanceExpr.java:287 
    /** @testcase qualified new expression using aspect type in method introduction body */
    public String TargetClass.addMethod() {
        String result = null;
        A a = A.aspectOf();
        result = a.getName();
        result = a.new inner().name(); // bug: remove this to avoid NPE
        return result;
    }
}
class TargetClass {}
