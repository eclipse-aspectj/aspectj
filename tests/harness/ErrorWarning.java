import org.aspectj.testing.Tester;

public class ErrorWarning {
    public static void main (String[] args) {
        boolean passed = true;
        try { ok(); } 
        catch (Error e) { passed = false; }
        Tester.check(passed, "did not catch error");
    } // end of main ()
    
    public static void ok() {
        try {
            throw new Error();;   // CE 13 unless -lenient
        } catch(Error e) { }      // CW 14 per aspect
    }
    static aspect A {
        declare warning : withincode(void ErrorWarning.ok())
            && (handler(Error)) : "warning";
    }
}


