import org.aspectj.testing.*;

public class AroundCall {
    void a(int nullm) { b(); }
    void b() { 
        Tester.check(false, "around failed");
    } 

    public static void main(String[] args) {
        AroundCall t = new AroundCall();
        t.a(42);
        Tester.checkAllEvents();
    }
    static {
        Tester.expectEvent("around");
    }
}

aspect TestAspect {
    /** @testcase PR#666 name binding in around cflow */
    void around(final int n) : // no bug if before advice
        cflow(execution(void AroundCall.a(int)) && args(n))  // no bug if no args
        && target(AroundCall) && !initialization(new(..))
        { 
            Tester.event("around");
            if (n > 100) proceed(n);  // some bugs hidden without call to proceed
        }
}
