package samenames;

import org.aspectj.testing.Tester;

public class Main {
    static Main samenames = new Main();
    public static void main(String[] args) {
        samenames.realMain(args);
    }
    void realMain(String[] args) {
        Tester.checkEqual(1, Other.returns1());
        Tester.checkEqual(2, new Other().returns2());
    }
}
