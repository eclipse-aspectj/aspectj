
import org.aspectj.testing.Tester;

// PR#114, PR#115

public class OrderOfCatches {
    public static void main(String[] args) { test(); }
    public static void test() {
        A a = new A();
        try {
            a.bar();
            Tester.check( true, "" );
        }
        catch ( FooException fe ) {
        }
        Tester.checkEqual(a.s, "a-FooException-EXC:FooException", "");
    }
}

class A {
    public String s = "a";
    void bar() throws FooException {
        foo();
    }
    void foo() throws FooException {
        throw new FooException();
    }
}

aspect AA {
    pointcut m(A a): target(a) && call(void bar());
    after (A a) throwing (FooException e): m(a) {
	a.s += "-" + e.getClass().getName();
    }
    after (A a) throwing (Exception e): m(a) {
	a.s += "-" + "EXC:"+e.getClass().getName();
    }
}

class FooException extends Exception {}
