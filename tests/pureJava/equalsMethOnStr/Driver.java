import org.aspectj.testing.Tester;

public class Driver {
    private static java.util.Vector v = new java.util.Vector();
    
    public static void main(String[] args) { test(); }

    public static void test() {
        Tester.check("foo".equals("foo"), "foo equals foo"); 
    }
}