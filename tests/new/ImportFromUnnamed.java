
import org.aspectj.testing.Tester;
import DeclaredExcs;

// PR#134

public class ImportFromUnnamed {
    public static void main(String[] args) { test(); }

    public static void test() {
        DeclaredExcs a = new DeclaredExcs();
        Tester.checkEqual(a.getClass().getName(), "DeclaredExcs", "class name");
    }
}
