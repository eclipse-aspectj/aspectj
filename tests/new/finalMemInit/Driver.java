
import org.aspectj.testing.Tester;

// PR#162
// works with JDK > 1.2 but not with JDK < 1.1.8

public aspect Driver {
    public static void main(String[] args) { test(); }
    public static void test() {
        Tester.checkEqual(new C().fi, 1, "");
    }    
    pointcut install(): execution(new()) && within(C); 
    /*static*/ after () : install() { }
}

class C { 
    public final int fi; 
    C() { 
        fi = 1; 
    } 
}
