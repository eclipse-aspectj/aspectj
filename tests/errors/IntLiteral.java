import org.aspectj.testing.*;

public class IntLiteral {
    public static void main(String[] args) {
        new IntLiteral().go();
        org.aspectj.testing.Tester.check(goRan, "Method go did not run");
    }

    static boolean goRan = false;

    void go() {
        goRan = true;
    }
}

aspect A {
    pointcut p1(int i) : execution(* *(5));
}
