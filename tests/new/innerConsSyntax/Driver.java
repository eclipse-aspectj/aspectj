
import org.aspectj.testing.Tester;

// PR#192

public class Driver {
    private static java.util.Vector v = new java.util.Vector();
    
    public static void main(String[] args) { test(); }

    public static void test() {
        Driver temp = new Driver();
        
        Inner inner = temp.new Inner();    
        
        Tester.check(inner.isInst, "Inner instance flag"); 
    }
    
    class Inner {
        public boolean isInst = true;
    }
}