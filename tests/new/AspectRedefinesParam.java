
import org.aspectj.testing.Tester;

/**
 * Test for: PR #65
 */

public aspect AspectRedefinesParam {
    public static void main(String[] args) { test(); }

    public static void test() {
        Tester.checkEqual(new Foo().b("a"), "a", "b('a')");
    }

    pointcut ccut(String s): this(Foo) && call(String b(String)) && args(s);

    before(String methodString): ccut(methodString) {
            String s;
            s = "b";
            methodString += s;
    }
}

class Foo {
    String b( String s ) {
        return s;
    }
}
