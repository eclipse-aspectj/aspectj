/**
 * this class tries to contain an error that ajc will miss, and that will
 * fall through to javac.
 * this will need to be steadily updated as ajc catches more things
 * the test is to be sure this isn't a silent error
 */

public class FromJavac {
    public static void foo() {
	int x;
	int y = x;
    }
}

