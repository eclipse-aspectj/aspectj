import org.aspectj.testing.Tester;

public class Proceeding3 {
    public static void main(String[] args) {
    }
    static aspect A {
        void around() : execution(void main(String[])) {
            proceed(); // special form or Proceeding.proceed()?
        }
    }
    void proceed() {
        Tester.checkFailed("A bare call to proceed inside around advice should never refer to a method");
    }
}