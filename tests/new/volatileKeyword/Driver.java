
import org.aspectj.testing.Tester;

public class Driver {
    volatile static boolean completed = true;
    public static void main(String[] args) { test(); }
    public static void test() {
        Tester.check(completed, "static volatile filed");
        Tester.check(new C().completed, "instance of volatile filed");
    }    
}

class C {
    volatile boolean completed = true;
}