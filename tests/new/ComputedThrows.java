import org.aspectj.testing.Tester;

public class ComputedThrows {
    public static void main(String[] args) { test(); }
    public static void test() {
        new ComputedThrows().bar();
        Tester.check("ran bar");
        Tester.check("caught Exception");
    }
    void bar() throws Exception {
        Tester.note("ran bar");
        throw new Exception();
    }
}

aspect Aspect {
    pointcut Foo(): within(ComputedThrows) && call(* ComputedThrows.bar(..));

    declare soft: Exception: Foo();

    void around(): Foo() {
        try {
            proceed();
        } catch (Exception e) {
            Tester.note("caught Exception");
        }
    }
}
