import org.aspectj.testing.Tester;

public class TopLevelBefore {
    public static void main(String[] args) {
        Tester.check(false, "should not have compiled");
    }
}

before(): {}
