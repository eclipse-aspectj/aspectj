
import org.aspectj.testing.Tester;

// PR#140

public class Driver {
    public static void main(String[] args) { test(); }
    public static void test() {
        Tester.checkEqual(new Foo().doIt(), 5, "min of 5 and 6");
        Tester.checkEqual(new Bar().doIt(), 1, "min of 5 and 2");
    }
}

class Foo {
    public static int doIt() {
        return new Foo().bar(1, 2, 3);
    }

    int bar(int a, int b, int c) { 
        return a * b * c;
    }
}

aspect FooAspect {
    pointcut cut() : target(Foo) && call(int bar(int, int, int));
    int around(): cut() {
        int originalResult = proceed();
        return Math.max(0, Math.min(originalResult, 5));
    } 
}

class Bar {
    public static int doIt() {
        return new Bar().bar(1, 2, 1);
    }

    int bar(int a, int b, int c) { 
        return a * b * c;
    }
}

aspect BarAspect {
    pointcut cut(Bar b) : target(b) && call(int bar(int, int, int));
    int around(Bar b): cut(b) {
        int originalResult = proceed(b);
        return Math.max(0, Math.min(originalResult, 1));
    }
}
