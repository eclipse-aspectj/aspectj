
package packageOne;

import org.aspectj.testing.Tester;

public class Main {
    public static void main(String[] args) {
        Tester.expectEvent("go");
        for (int i = 0; i < args.length; i++) {
            Tester.expectEvent(args[i]);
        }
        go();
        Tester.checkAllEvents();
    }
    static void go() {
        Tester.event("go");
        Tester.event("new-event"); // newly-generated event
    }
}