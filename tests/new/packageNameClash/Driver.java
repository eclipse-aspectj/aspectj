import org.aspectj.testing.Tester;

public class Driver {
    public static void main(String[] args) { test(); }

    public static void test() {
	new pkg.Class1().doIt();
    }
}
