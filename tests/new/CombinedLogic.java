import org.aspectj.testing.Tester;

/**
 * Test for: PR #99
 */

public class CombinedLogic {
    public static void main(String[] args) { test(); }

    public static void test() {
	Object foo = null;
	int baz = 1;
	int foobaz = baz;

	if ((null == foo) && (baz != foobaz)) {
	    Tester.check(false, "better not");
	} else {
	    Tester.check(true, "it better be true");
	}
    }
}
