
import org.aspectj.testing.Tester;

/** Drive AllRuntime in a tester environment */
public class TesterDriver {
    public static void main(String[] args) {
        StringBuffer sb = new StringBuffer();
        AllRuntime.resultCache(sb);
        int errors = AllRuntime.driveTest();
        if (0 != errors) {
            Tester.check(0 == errors, "AllRuntime errors: " + errors);
            System.err.println(sb.toString());
        }
    }
}
