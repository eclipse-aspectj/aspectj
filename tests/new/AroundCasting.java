import org.aspectj.testing.Tester;

public class AroundCasting {
    public static void main(String[] args) {
        Tester.checkEqual(x = 3, 1003);
        Tester.checkEqual(x, 3);
        Tester.checkEvents(new String[] { "enter main" });
    }
    static int x;
}


aspect A {
    static boolean test() { return true; }

    int around(): if (test()) && set(int AroundCasting.x) {
        return proceed() + 1000;
    }

    void around(): execution(void AroundCasting.main(String[])) {
        Tester.event("enter main");
        proceed();
    }
}
