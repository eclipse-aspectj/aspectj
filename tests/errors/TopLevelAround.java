import org.aspectj.testing.Tester;

void around(): call(void main(..)) {} // CE 3

public aspect TopLevelAround {
    public static void main(String[] args) {
        Tester.check(false, "should not have compiled");
    }
}

