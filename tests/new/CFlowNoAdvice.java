import org.aspectj.testing.Tester;

public class CFlowNoAdvice {
    public static void main(String[] args) {
        new Foo().bar();
        new Foo().bar();
    }
}

class Foo {
    void bar() {
        Tester.check(CFlowAspect.aspectOf() != null, "cflow created");
    }
}




aspect CFlowAspect percflow(CFlowAspect.contexts()) {
    pointcut contexts(): target(Foo) && call(void bar());
}
