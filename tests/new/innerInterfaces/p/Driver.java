package p;

import org.aspectj.testing.Tester;
import other.Test;

public class Driver {
    public static void main(String[] args) {
        Test t = new Test();
        t.foo();
        Tester.checkEqual(InnerTest.getCallCount(t), 1);
        t.foo();
        Tester.checkEqual(InnerTest.getCallCount(t), 2);
    }
}
