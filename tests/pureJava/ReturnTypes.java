import org.aspectj.testing.Tester;

public class ReturnTypes {
    public static void main(String[] args) {
        Tester.check(convertDouble("2") == 0.0, "return types");
    }

    static double convertDouble(Object o) {
        return 0;
    }
}
