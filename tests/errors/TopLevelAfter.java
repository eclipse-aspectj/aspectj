import org.aspectj.testing.Tester;

public class TopLevelAfter {
    public static void main(String[] args) {
        Tester.check(false, "should not have compiled");
    }
}

after(): {}
