import org.aspectj.testing.Tester;

public class AroundCasting {
    public static void main(String[] args) {
        Tester.checkEqual(x = 3, 3);
        Tester.checkEqual(x, 1003);
        Tester.checkEvents(new String[] { "enter main" });
    }
    static int x;
}


aspect A {
    static boolean test() { return true; }

    int around(): if (test()) && get(int AroundCasting.x) {
        return proceed() + 1000;
    }

    void around(): execution(void AroundCasting.main(String[])) {
        Tester.event("enter main");
        proceed();
    }
}
