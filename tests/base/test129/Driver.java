import org.aspectj.testing.Tester;

public class Driver {
    public static void main(String[] args) { test(); }
  
    public static void test() {
        AbstractC aC = new C();
        Tester.checkEqual(aC.foo(), 42, "introduced abstract");
        Tester.checkEqual(((I)aC).bar(), 12, "introduced on interface");
    }
}

interface I {
}

abstract class AbstractC implements I {
}

class C extends AbstractC {
}

aspect A {
    //introduction AbstractC {
    abstract int AbstractC.foo();
    //}

    //introduction C {
    int C.foo() { return 42; }
    //}

    //introduction I {
    public int I.bar() { return 12; }
    //}
}
