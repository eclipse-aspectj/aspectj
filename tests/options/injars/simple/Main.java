

import org.aspectj.testing.Tester;

public class Main {
    public static void main(String[] args) {
        Tester.expectEvent("run");
        new Main().run();
        Tester.checkAllEvents();
    }
    public void run(){}
}