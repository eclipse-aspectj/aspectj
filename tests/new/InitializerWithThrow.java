import org.aspectj.testing.Tester;

public class InitializerWithThrow {
    public static void main(String[] args) {
        try {
            new InitializerWithThrow();
            Tester.check(false, "no exception");
        } catch (TestException te) {
        }
    }

    static class TestException extends Exception {}

    int i = ini();

    static int ini() throws TestException {
	throw new TestException();
    }

    InitializerWithThrow() throws TestException {}

}
