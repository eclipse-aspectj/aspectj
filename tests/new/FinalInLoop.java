import org.aspectj.testing.Tester;

public class FinalInLoop {
    /** @testcase PR#709 PUREJAVA final assignment in loop */
    public static void main (String[] args) {
        for (int i = 0; i < 1; i++) {
            final String s;
            if (true) {
                s = "true";
            } else {
                s = "false";
            }
            Tester.check("true".equals(s), "s not true");
        }
    }
}
