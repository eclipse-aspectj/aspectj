
import org.aspectj.testing.Tester;

public class Driver {
    private static java.util.Vector v = new java.util.Vector();
    
    public static void main(String[] args) { test(); }

    public static void test() {
        v.addElement("foo");
        boolean containsFoo = v.contains("foo");
        Object v = new Object();
        
        Tester.check(containsFoo, "Vector contains element added"); 
    }
}
