import org.aspectj.testing.*;

public class StrayDot {
    public static void main(String[] args) {
        new StrayDot().realMain(args);
    }
    
    public void realMain(String[] args) {
        .int i = 13;
        int. z = 13;
        int .j = 13;
        int k. = 13;
        int l .= 13;
        int m =. 13;
        int n = .13;
        Tester.check(false, "Shouldn't have compiled");
    }
}

