import org.aspectj.testing.Tester;
import java.io.*;

/**
 * Test for: PR #98
 */

public class IntroductionOfInitializer {
    public static void main(String[] args) { test(); }

    public static void test() {
        Tester.checkEqual(Foo.a, "class", "zero instances");
        Foo foo = new Foo();
        Tester.checkEqual(Foo.a, "class-instance", "one instances");
        foo = new Foo();
        Tester.checkEqual(Foo.a, "class-instance-instance", "two instances");
    }
}

aspect A {
    private static String classValue = "class";
    private static String instanceValue = "-instance";

    after(): staticinitialization(Foo) {
	Foo.a += classValue;
    }

    after(): initialization(Foo.new(..)) {
	Foo.a += instanceValue;
    }
}


class Foo {	
    static String a = "";
}
