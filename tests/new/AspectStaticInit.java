
import org.aspectj.testing.Tester;

/** @testcase PR#804 aspect static initializers should run before instance constructed */
public class AspectStaticInit {
    public static void main(String[] args) {
        Tester.check(A.i == 1, "1 != A.i=" + A.i);
    }
}
// XXX need tests for other instantiations besides issingleton
aspect A {
    static int i = 1;
    A() {
        Tester.check(i == 1, "1 != i=" + i);
    }
}
