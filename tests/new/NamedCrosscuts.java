import org.aspectj.testing.Tester;

public class NamedCrosscuts {
    crosscut fooCut(Foo f): void foo() && f;

    static advice(Foo f): fooCut(f) {
        before {
            System.err.println("before advice");
        }
    }

    crosscut allMethodsCut(): * && !(NamedCrosscuts) && !abstract * *(..);

    static advice(): allMethodsCut() {
        before {
            System.err.println("method: "+thisJoinPoint.methodName);
        }
    }

    public static void test() {
        new Foo().foo();
    }

    public static void main(String[] args) {
        test();
    }
}

class Foo {
    void foo() {}
}
