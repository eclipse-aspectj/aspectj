import org.aspectj.testing.Tester;

public class Driver {
    public static void main(String[] args) { test(); }

    public static void test() {
        C c = new C();
        Tester.check(c == c.foo(), "this wasn't this");
    }
}

class C { }

aspect A {
    //introduction C {
    C C.foo() { return this; }
    //}
}
    
