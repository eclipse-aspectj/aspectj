
package packageOne;

import org.aspectj.testing.Tester;

public class Main {
    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            Tester.expectEvent(args[i]);
        }
        Tester.event("in packageOne.Main.main(..)");
        Tester.checkAllEvents();
    }
}

