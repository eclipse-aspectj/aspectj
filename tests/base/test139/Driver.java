// inner aspects and around

import org.aspectj.testing.Tester;

public class Driver {
    public static void test() {
        C2 c2 = new C2();

        Tester.checkEqual(c2.foo(), 142, "modified c2.foo()");
    }

    public static void main(String[] args) { test(); }
}


class C1 {
    private int myInteger = 100;

    static aspect A {
         int around(C2 c2): 
                target(c2) && call(int foo()) {
            int result = proceed(c2);
            return result + c2.getC1().myInteger;
        }
    }
}

class C2 {
    public C1 getC1() {
        return new C1();
    }
    int foo() {
        return 42;
    }
}

