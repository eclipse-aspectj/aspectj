import org.aspectj.testing.Tester;

before(): call(void main(..)) {} // CE 3

public class TopLevelBefore {
    public static void main(String[] args) {
        Tester.check(false, "should not have compiled");
    }
}
