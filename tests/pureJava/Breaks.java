import org.aspectj.testing.Tester;

public class Breaks {
    static boolean sawTrue, sawFalse;
    
    public static void main(String[] args) {
        m(true);
        Tester.check(sawTrue, "true");
        Tester.check(!sawFalse, "!false");

    }

    static void m(boolean t) {
    BLOCK: {
        if (t) {
            sawTrue = true;
            System.out.println("true");
            break BLOCK;
        } else {
            sawFalse = true;
            System.out.println("false");
        }
    }
    }

    static int m1(boolean t) {
    loop: while (true) {
        if (t) break loop;
    }
    return 1;
    }

    static int m2(boolean t) {
        while (true) {
            if (t) break;
        }
        return 1;
    }
}
