
package testaspect;

import org.aspectj.testing.Tester;

public aspect SignalMainRunnable {

    /**
     * Signal Tester event "aspect" if a main method
     * calls run() on a Runnable.
     */
    after() returning : withincode(public static void main(..))
        && target(Runnable) && call(void run()) {
        Tester.event("aspect");
    }
}

