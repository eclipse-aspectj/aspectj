
import org.aspectj.testing.Tester;

// PR#209

public aspect BadPointcutName {
    private static java.util.Vector v = new java.util.Vector();
    
    public static void main(String[] args) { test(); }

    public static void test() {
        Tester.check(true, ""); 
    }
    
    /*static*/ after() returning (): noSuchCut() {}
}

