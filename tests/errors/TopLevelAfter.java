import org.aspectj.testing.Tester;

after(): call(void main(..)) {} // CE 3

public class TopLevelAfter {
    public static void main(String[] args) {
        Tester.check(false, "should not have compiled");
    }
}

