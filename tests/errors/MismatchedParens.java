
import org.aspectj.testing.Tester;

// PR#213

public class MismatchedParens {

    public static void main(String[] args) { test(); }

    public static void test() {
        org.aspectj.testing.Tester.check(true, ""); 
    }
}

aspect A {
    /*static*/ before(): foocut(
}
