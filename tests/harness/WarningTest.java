import org.aspectj.testing.Tester;

public class WarningTest {
    public static void main (String[] args) {
        boolean passed = true;
        try { ok(); } 
        catch (Error e) { passed = false; }
        Tester.check(passed, "did not catch error");
    } 
    
    public static void ok() {
        try {
            throw new Error();
        } catch(Error e) { }      // CW 14 per aspect
    }
    static aspect A {
        declare warning : withincode(void WarningTest.ok())
            && (handler(Error)) : "warning";
    }
}


