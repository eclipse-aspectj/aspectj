import org.aspectj.testing.Tester;

public class Proceeding2 {
    public static void main(String[] args) {
        Tester.checkFailed("Proceed with a receiver should be treated as a method call, not the special form");
    }
    static aspect A {
        void around() : execution(void main(String[])) {
            Proceeding2.proceed(null); // BUG: treated as proceed(Object);
        }
    }
    static void proceed(Object o) {}
}