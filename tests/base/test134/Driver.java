import org.aspectj.testing.Tester;

public class Driver {
    public static void main(String[] args) { test(); }
    public int divide(int x, int y) {
        return x/y;
    }

    public static void test() {
        Tester.checkEqual(new Driver().divide(6, 3), 2, "divide(6, 3)");
        Tester.checkEqual(new Driver().divide(6, 0), -1, "divide(6, 0)");
    }
}

aspect CatchArithmetic {
    int around(): target(*) && call(int *(..)) {
        int x;
        try {
            x = proceed();
        }
        catch (ArithmeticException e) {
            return -1;
        }
        return x;
    }
}
