import org.aspectj.testing.Tester;

public class Driver {
    public static void main(String[] args) { test(); }

    public static void test() {
        new C().foo("hello");    
    }
}

class C {
    void foo(String s) {
        Tester.checkEqual(s, "hello", "in method body");
    }
}

aspect A {
     before(String str): target(C) && call(void foo(String)) && args(str) {
        Tester.checkEqual(str, "hello", "in advice body");     
    }
}
