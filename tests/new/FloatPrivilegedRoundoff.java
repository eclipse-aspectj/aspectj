import org.aspectj.testing.Tester;

public class FloatPrivilegedRoundoff {
    private static float f = 0.3f;

    public static void main(String[] args) {
        Tester.check(A.foo() == 0.3f, "didn't return original");
        Tester.check(f == 0.3f + 1, "didn't increment");
    }
}

privileged aspect A {
    static float foo() {
        return (FloatPrivilegedRoundoff.f)++;
    }
}
