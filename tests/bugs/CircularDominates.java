
import org.aspectj.testing.Tester;
import java.util.*;

/** @testcase PR#902 circularity in declare dominates */
public class CircularDominates {

    public static void main(String[] args) {
		foo();
		throw new Error("expected compiler error");
    }

    public static void foo() {
    }
}

aspect BugDemoAspect {
    declare dominates : B, A, B; // CE 18
}

aspect A {
    before() : target(CircularDominates) && call(static void foo(..)) {
    }
}

aspect B {
    before() : cflowbelow(execution(static void main(String[]))) {
    }
}
