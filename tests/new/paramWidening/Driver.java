
import org.aspectj.testing.Tester;

// PR#195

public class Driver {
    private static java.util.Vector v = new java.util.Vector();
    
    public static void main(String[] args) { test(); }

    public static void test() {
        long l = foo(42);
        
        Tester.check(l == 42, "foo(42) == 42"); 
    }
    
    private static float foo(float f) {
        return f;
    }

    private static long foo(long l) {
        return l;
    }
}