
import org.aspectj.testing.Tester;

/** @testcase VerifyError after around advice falls off end of tryCatch */
public class TryOffEnd {
    public static void main(String[] args) {
        Tester.check(new TryOffEnd().error(), "functional failure");
    }
    public boolean error() {
        String s = null;
        try {
            s = System.getProperty("unknown Property");
        } catch (Throwable t) {  // CW 13 cannot apply advice
            t.printStackTrace(System.err);
        }
        return true;
    }
}

aspect A {
    Object around() : within(TryOffEnd) && handler(Throwable) {  // CW 21 cannot apply advice
        Object result =  proceed();
        return result;
    }
}
