package test;

import org.aspectj.testing.*;

public class Test {
    public static void main(String[] args) {
        new Test().run();
        Tester.checkAllEvents();
    }
    public void run() {
        Tester.event("run");
    }
    static {
        Tester.expectEventsInString("before,after,run");
    }
}
