

import org.aspectj.testing.Tester; 

public class Util {
    public static void fail(String s) {
        //System.err.println("fail: " + s);
        Tester.check(false,s);
    }
    public static void signal(String s) {
        //System.err.println("signal: " + s);
        Tester.event(s);
    }
}
