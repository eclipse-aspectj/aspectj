import org.aspectj.testing.Tester;

public class Driver {
    public static void main(String[] args) { test(); }
    public static void test() {
        C c = new C();

        Tester.checkEqual(c.basic(), 4, "basic()");
        Tester.checkEqual(c.exceptional(), 3, "exceptional()");
    }
}

class C {
    public int basic() {
        return 1;
    }

    public int exceptional() {
        return 1;
    }
}

aspect B {
     int around(): target(C) && call(int basic()) {
        return 4;
    }
     int around(): target(C) &&  call(int exceptional()) {
        return 3;
    }
}

