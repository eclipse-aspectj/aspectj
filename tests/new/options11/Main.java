

import org.aspectj.testing.Tester;

public class Main {
    static int i = 0;
    public static void main(String[] args) {
        new injar.Injar().run();
        Tester.check(i != 0, "aspect failed");
    }   
}