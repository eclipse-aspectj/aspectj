
import org.aspectj.testing.Tester;
import org.aspectj.testing.Tester; 

public class Common {
    public static String[] SIGNALS = new String[] { };
    public static final void expect(String[] args) { 
        for (int i = 0; i < args.length; i++) {
            expect(args[i]); 
        }     
    }
    public static final void expect(String s) { Tester.expectEvent(s); }
    public static final void signal(String s) { Tester.event(s); }
    public static final void fail(String s) { Tester.check(false, s); }
    public static final void check() { Tester.checkAllEvents(); }
    public static final void joinWith(Thread thread) {
        if (null == thread) {
            Common.fail("null thread");
        }
        try { thread.join(); } 
        catch (InterruptedException e) {
            Common.fail("Interrupted");
        }
    }
}

class Target {
    public static void main (String[] args) {
        Common.expect(Target.SIGNALS);
        Common.expect(Aspect.SIGNALS);
        int result = new Target().targetMethod();
        if (1 != result) Common.fail("1 != result: " + result);
        Common.check();
    } 
    pointcut pointcutTarget() : call(int Target.targetMethod());
    public static String[] SIGNALS = new String[]
    { "targetMethod()" };
    public int targetMethod() {
        Common.signal(SIGNALS[0]);
        return 1;
    }
}
