
import org.aspectj.testing.Tester;

// PR#151

public class Driver {
    public static void main(String[] args) { test(); }
    public static void test() {
        Tester.checkEqual(C.set(), "C-advised-set", "");
    }    
    
    static advice(C c): c && * *(..) {
        before {
            if (c != null) 
                Tester.check(false, "c == null");
            else 
    	        c.s += "-advised";
        }
    } 
}

class C {
    static String s = "C";
    
    static String set() {
        s += "-set";
        return s;
    }
}
