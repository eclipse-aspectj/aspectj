// PUREJAVA: Corrrect supercall lookup for method().name()
import org.aspectj.testing.Tester;

public class Driver {
    private Foo foo = new Foo();
    
    public static void main(String[] args) { test(); }
                                             
    public Foo getFoo() { return foo; }

    public String bar() {
        return getFoo().bar();
    }

    public static void test() {
        Tester.checkEqual(new Driver().bar(), "Foo", "getFoo().bar()");
    }
}

class Foo {
    public String bar() {
        return "Foo";
    }
}
