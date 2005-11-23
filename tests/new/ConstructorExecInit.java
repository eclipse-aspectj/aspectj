import org.aspectj.testing.*;

/**
 * -usejavac mode: no error
 * not -usejavac mode: VerifyError
 */
public class ConstructorExecInit {
    public static void main(String[] args) {
        new ConstructorExecInit();
        Tester.checkAllEvents();
    }
    static {
        Tester.expectEvent("execution");
        Tester.expectEvent("initialization");
    }
}

/** @testcase after returning from initialization and after executing constructor */
aspect A {
    after (Object target) : execution(*.new(..)) && target(target) && !within(A) { 
        Tester.event("execution");
    }
    after () returning : initialization(new(..)) && !this(A) { 
        Tester.event("initialization");
    }
}
