
package main;

import org.aspectj.testing.Tester;

public class Main {
    public static void main (String[] args) {
        new Target().run();
        Tester.expectEvent("Target.run()");
        Tester.checkAllEvents();
    }
}
