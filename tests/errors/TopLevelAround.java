import org.aspectj.testing.Tester;

public class TopLevelAround {
    public static void main(String[] args) {
        Tester.check(false, "should not have compiled");
    }
}

around(): {} // CE 9
