import org.aspectj.testing.Tester;

public class DirChangesTest {
    public static void main (String[] args) {
        Tester.check(true, "ok");
    }
    static class Nested {
        static int meaningOfLife = 42;
    }
    class Inner {
        int meaningOfLife = 42;
    }
    
}


