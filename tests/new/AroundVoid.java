import org.aspectj.testing.Tester;

public class AroundVoid {
    public static void main(String[] args) {
        C c = new C();
        try {
            c.m1();
        } catch (RuntimeException exc) {
            Tester.event("caught RuntimeException");
        }
        c.m2();
        c.m3(true);
        c.m4(true);
        try {
            c.m5();
        } catch (ArithmeticException exc) {
            Tester.event("caught ArithmeticException");
        }
        c.m6();

        //Tester.printEvents();
        Tester.checkEventsFromFile("AroundVoid.out");
    }
}

class C {
    void m1() {
        throw new RuntimeException("m1");
    }

    void m2() {
        Tester.event("m2");
    }

    void m3(boolean test) {
        if (test) {
            return;
        } else {
            return;
        }
    }

    void m4(boolean test) {
        if (test) {
            return;
        } else {
            Tester.event("false");
        }
    }

    void m5() {
        while (true) {
            int x = 0;
            int y = 2/x;
            throw new ArithmeticException();
        }
    }

    void m6() {}
}

aspect A {
    Object around(): execution(void C.m*(..)) {
        Tester.event(thisJoinPoint.toShortString());
        return proceed();
    }
}
