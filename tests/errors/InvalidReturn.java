package errors;

public class InvalidReturn {
    public int doNothing() { return 0; }
    public static void test() {}
}

aspect C {
    pointcut iCut(): this(*) && call(int *(..));

    before(): iCut() {
        return -1;
    }

    after(): iCut() {
        return 1;
    }
    after() returning (): iCut() {
        return 1;
    }
    after() throwing (ArithmeticException e): iCut() {
        return -1;
    }
}
