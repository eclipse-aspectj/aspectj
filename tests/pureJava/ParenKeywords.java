import org.aspectj.testing.Tester;

public class ParenKeywords {
    public static void main(String[] args) {
	if ((false) || (true)) {
	    new ParenKeywords().foo();
	}
        Tester.checkEvents("foo");
    }
    void foo() {
	if ((this) == (null)) {
	    System.err.println("shouldn't happen");
	} else {
            Tester.event("foo");
        }
    }
}
