import org.aspectj.testing.Tester;

public class TryCatchFinally {
    public static void main(String[] args) {
        Tester.checkEqual(m1(), "good");
        Tester.checkEqual(m2(), "good");
        Tester.checkEqual(m3(), "good");

        try {
            m1v();
        } catch (RuntimeException re) {
            Tester.event("main-caught");
        }
        Tester.checkAndClearEvents(new String[] {
            "caught", "finally", "main-caught"} );
        try {
            m2v();
        } catch (RuntimeException re) {
            Tester.event("main-caught");
        }
        Tester.checkAndClearEvents(new String[] {
            "caught", "finally", "main-caught"} );

        m3v();
        Tester.checkAndClearEvents(new String[] {
            "caught", "finally"} );
    }

    public static String m1() {
        try {
            throw new RuntimeException("hi");
        } catch (RuntimeException er) {
            throw er;
        } finally {
            return "good";
        }
    }
    public static String m2() {
        try {
            return m1() + "XXX";
        } catch (RuntimeException er) {
            throw er;
        } finally {
            return "good";
        }
    }
    public static String m3() {
        try {
            throw new RuntimeException("hi");
        } catch (RuntimeException er) {
            return "bad-c";
        } finally {
            return "good";
        }
    }
    public static void m1v() {
        try {
            throw new RuntimeException("hi");
        } catch (RuntimeException er) {
            Tester.event("caught");
            throw er;
        } finally {
            Tester.event("finally");
        }
    }
    public static void m2v() {
        try {
            throw new RuntimeException("hi");
        } catch (RuntimeException er) {
            Tester.event("caught");
            throw er;
        } finally {
            Tester.event("finally");
        }
    }
    public static void m3v() {
        try {
            throw new RuntimeException("hi");
        } catch (RuntimeException er) {
            Tester.event("caught");
            return;
        } finally {
            Tester.event("finally");
        }
    }
}
