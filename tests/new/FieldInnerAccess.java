import org.aspectj.testing.Tester;

public class FieldInnerAccess {
    public static void main(String[] args) {
        Derived d = new Derived();
        d.m();
        Tester.checkAndClearEvents(new String[] {"lock: 1"});

        d.mi();
        Tester.checkAndClearEvents(new String[] {"lock: 1"});

        d.mib();
        Tester.checkAndClearEvents(new String[] {"lock: foo"});
    }
}

class Base {
    private static String lock = "foo";

    public void mib() {
        Runnable r = new Runnable() {
                public void run() {
                    Tester.event("lock: " + lock);
                }
            };

        r.run();
    }
}

class Derived extends Base {
    private static Object lock = new Integer(1);

    public void m() {
        Tester.event("lock: " + lock);
    }

    public void mi() {
        Runnable r = new Runnable() {
                public void run() {
                    Tester.event("lock: " + lock);
                }
            };

        r.run();
    }
}
